package top.vrcode.app.utils

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Environment
import android.system.Os
import android.util.Pair
import android.widget.Toast
import com.termux.shared.file.FileUtils
import com.termux.shared.file.TermuxFileUtils
import com.termux.shared.interact.MessageDialogUtils
import com.termux.shared.logger.Logger
import com.termux.shared.markdown.MarkdownUtils
import com.termux.shared.models.errors.Error
import com.termux.shared.packages.PackageUtils
import com.termux.shared.termux.TermuxConstants
import top.vrcode.app.Constant
import top.vrcode.app.R
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Install the Termux bootstrap packages if necessary by following the below steps:
 *
 *
 * (1) If $PREFIX already exist, assume that it is correct and be done. Note that this relies on that we do not create a
 * broken $PREFIX directory below.
 *
 *
 * (2) A progress dialog is shown with "Installing..." message and a spinner.
 *
 *
 * (3) A staging directory, $STAGING_PREFIX, is cleared if left over from broken installation below.
 *
 *
 * (4) The zip file is loaded from a shared library.
 *
 *
 * (5) The zip, containing entries relative to the $PREFIX, is is downloaded and extracted by a zip input stream
 * continuously encountering zip file entries:
 *
 *
 * (5.1) If the zip entry encountered is SYMLINKS.txt, go through it and remember all symlinks to setup.
 *
 *
 * (5.2) For every other zip entry, extract it into $STAGING_PREFIX and set execute permissions if necessary.
 */
internal object TermuxInstaller {
    private const val LOG_TAG = "TermuxInstaller"

    /**
     * Performs bootstrap setup if necessary.
     */
    fun setupBootstrapIfNeeded(activity: Activity, whenDone: Runnable) {
        val bootstrapErrorMessage: String

        // This will also call Context.getFilesDir(), which should ensure that termux files directory
        // is created if it does not already exist
        val filesDirectoryAccessibleError: Error? =
            TermuxFileUtils.isTermuxFilesDirectoryAccessible(activity, true, true)
        val isFilesDirectoryAccessible = filesDirectoryAccessibleError == null

        // Termux can only be run as the primary user (device owner) since only that
        // account has the expected file system paths. Verify that:
        if (!PackageUtils.isCurrentUserThePrimaryUser(activity)) {
            bootstrapErrorMessage = activity.getString(
                R.string.bootstrap_error_not_primary_user_message,
                MarkdownUtils.getMarkdownCodeForString(
                    TermuxConstants.TERMUX_PREFIX_DIR_PATH,
                    false
                )
            )
            Logger.logError(LOG_TAG, "isFilesDirectoryAccessible: $isFilesDirectoryAccessible")
            Logger.logError(LOG_TAG, bootstrapErrorMessage)
            MessageDialogUtils.exitAppWithErrorMessage(
                activity,
                activity.getString(R.string.bootstrap_error_title),
                bootstrapErrorMessage
            )
            return
        }
        if (!isFilesDirectoryAccessible) {
            bootstrapErrorMessage = """
                ${Error.getMinimalErrorString(filesDirectoryAccessibleError)}
                TERMUX_FILES_DIR: ${
                MarkdownUtils.getMarkdownCodeForString(
                    TermuxConstants.TERMUX_FILES_DIR_PATH,
                    false
                )
            }
                """.trimIndent()
            Logger.logError(LOG_TAG, bootstrapErrorMessage)
            MessageDialogUtils.showMessage(
                activity,
                activity.getString(R.string.bootstrap_error_title),
                bootstrapErrorMessage, null
            )
            return
        }

        // If prefix directory exists, even if its a symlink to a valid directory and symlink is not broken/dangling
        if (FileUtils.directoryFileExists(TermuxConstants.TERMUX_PREFIX_DIR_PATH, true)) {
            val PREFIX_FILE_LIST = TermuxConstants.TERMUX_PREFIX_DIR.listFiles()
            // If prefix directory is empty or only contains the tmp directory
            if (PREFIX_FILE_LIST == null || PREFIX_FILE_LIST.size == 0 || PREFIX_FILE_LIST.size == 1 && TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH == PREFIX_FILE_LIST[0].absolutePath) {
                Logger.logInfo(
                    LOG_TAG,
                    "The termux prefix directory \"" + TermuxConstants.TERMUX_PREFIX_DIR_PATH + "\" exists but is empty or only contains the tmp directory."
                )
            } else {
                whenDone.run()
                return
            }
        } else if (FileUtils.fileExists(TermuxConstants.TERMUX_PREFIX_DIR_PATH, false)) {
            Logger.logInfo(
                LOG_TAG,
                "The termux prefix directory \"" + TermuxConstants.TERMUX_PREFIX_DIR_PATH + "\" does not exist but another file exists at its destination."
            )
        }
        val progress = ProgressDialog.show(
            activity,
            null,
            activity.getString(R.string.bootstrap_installer_body),
            true,
            false
        )
        object : Thread() {
            override fun run() {
                try {
                    Logger.logInfo(
                        LOG_TAG,
                        "Installing " + TermuxConstants.TERMUX_APP_NAME + " bootstrap packages."
                    )
                    var error: Error?

                    // Delete prefix staging directory or any file at its destination
                    error = FileUtils.deleteFile(
                        "termux prefix staging directory",
                        TermuxConstants.TERMUX_STAGING_PREFIX_DIR_PATH,
                        true
                    )
                    if (error != null) {
                        showBootstrapErrorDialog(
                            activity,
                            whenDone,
                            Error.getErrorMarkdownString(error)
                        )
                        return
                    }

                    // Delete prefix directory or any file at its destination
                    error = FileUtils.deleteFile(
                        "termux prefix directory",
                        TermuxConstants.TERMUX_PREFIX_DIR_PATH,
                        true
                    )
                    if (error != null) {
                        showBootstrapErrorDialog(
                            activity,
                            whenDone,
                            Error.getErrorMarkdownString(error)
                        )
                        return
                    }

                    // Create prefix staging directory if it does not already exist and set required permissions
                    error = TermuxFileUtils.isTermuxPrefixStagingDirectoryAccessible(true, true)
                    if (error != null) {
                        showBootstrapErrorDialog(
                            activity,
                            whenDone,
                            Error.getErrorMarkdownString(error)
                        )
                        return
                    }

                    // Create prefix directory if it does not already exist and set required permissions
                    error = TermuxFileUtils.isTermuxPrefixDirectoryAccessible(true, true)
                    if (error != null) {
                        showBootstrapErrorDialog(
                            activity,
                            whenDone,
                            Error.getErrorMarkdownString(error)
                        )
                        return
                    }
                    Logger.logInfo(
                        LOG_TAG,
                        "Extracting bootstrap zip to prefix staging directory \"" + TermuxConstants.TERMUX_STAGING_PREFIX_DIR_PATH + "\"."
                    )
                    val buffer = ByteArray(8096)
                    val symlinks: MutableList<Pair<String, String>> = ArrayList(50)
                    val zipBytes = loadZipBytes(activity)
                    ZipInputStream(ByteArrayInputStream(zipBytes)).use { zipInput ->
                        var zipEntry: ZipEntry
                        while (zipInput.nextEntry.also { zipEntry = it } != null) {
                            if (zipEntry.name == "SYMLINKS.txt") {
                                val symlinksReader = BufferedReader(InputStreamReader(zipInput))
                                var line: String
                                while (symlinksReader.readLine().also { line = it } != null) {
                                    val parts = line.split("←").toTypedArray()
                                    if (parts.size != 2) throw RuntimeException("Malformed symlink line: $line")
                                    val oldPath = parts[0]
                                    val newPath =
                                        TermuxConstants.TERMUX_STAGING_PREFIX_DIR_PATH + "/" + parts[1]
                                    symlinks.add(Pair.create(oldPath, newPath))
                                    error = ensureDirectoryExists(File(newPath).parentFile!!)
                                    if (error != null) {
                                        showBootstrapErrorDialog(
                                            activity,
                                            whenDone,
                                            Error.getErrorMarkdownString(error)
                                        )
                                        return
                                    }
                                }
                            } else {
                                val zipEntryName = zipEntry.name
                                val targetFile = File(
                                    TermuxConstants.TERMUX_STAGING_PREFIX_DIR_PATH,
                                    zipEntryName
                                )
                                val isDirectory = zipEntry.isDirectory
                                error =
                                    ensureDirectoryExists(if (isDirectory) targetFile else targetFile.parentFile)
                                if (error != null) {
                                    showBootstrapErrorDialog(
                                        activity,
                                        whenDone,
                                        Error.getErrorMarkdownString(error)
                                    )
                                    return
                                }
                                if (!isDirectory) {
                                    FileOutputStream(targetFile).use { outStream ->
                                        var readBytes: Int
                                        while (zipInput.read(buffer)
                                                .also { readBytes = it } != -1
                                        ) outStream.write(buffer, 0, readBytes)
                                    }
                                    if (zipEntryName.startsWith("bin/") || zipEntryName.startsWith("libexec") ||
                                        zipEntryName.startsWith("lib/apt/apt-helper") || zipEntryName.startsWith(
                                            "lib/apt/methods"
                                        )
                                    ) {
                                        Os.chmod(targetFile.absolutePath, 448)
                                    }
                                }
                            }
                        }
                    }
                    if (symlinks.isEmpty()) throw RuntimeException("No SYMLINKS.txt encountered")
                    for (symlink in symlinks) {
                        Os.symlink(symlink.first, symlink.second)
                    }
                    Logger.logInfo(LOG_TAG, "Moving termux prefix staging to prefix directory.")
                    if (!TermuxConstants.TERMUX_STAGING_PREFIX_DIR.renameTo(TermuxConstants.TERMUX_PREFIX_DIR)) {
                        throw RuntimeException("Moving termux prefix staging to prefix directory failed")
                    }
                    Logger.logInfo(LOG_TAG, "Bootstrap packages installed successfully.")
                    activity.runOnUiThread(whenDone)
                } catch (e: Exception) {
                    showBootstrapErrorDialog(
                        activity,
                        whenDone,
                        Logger.getStackTracesMarkdownString(
                            null,
                            Logger.getStackTracesStringArray(e)
                        )
                    )
                } finally {
                    activity.runOnUiThread {
                        try {
                            progress.dismiss()
                        } catch (e: RuntimeException) {
                            // Activity already dismissed - ignore.
                        }
                    }
                }
            }
        }.start()
    }

    private fun loadZipBytes(activity: Activity): ByteArray? {
        val inputStream = activity.assets.open(Constant.TERMUX_BOOTSTRAP_FILENAME)
        val buffer = ByteArrayOutputStream()
        var nRead: Int
        val data = ByteArray(16384)
        while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
        }
        return buffer.toByteArray()
    }

    fun showBootstrapErrorDialog(activity: Activity, whenDone: Runnable?, message: String?) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    fun setupStorageSymlinks(context: Context) {
        val LOG_TAG = "termux-storage"
        Logger.logInfo(LOG_TAG, "Setting up storage symlinks.")
        object : Thread() {
            override fun run() {
                try {
                    val error: Error?
                    val storageDir = TermuxConstants.TERMUX_STORAGE_HOME_DIR
                    error = FileUtils.clearDirectory("~/storage", storageDir.absolutePath)
                    if (error != null) {
                        Logger.logErrorAndShowToast(context, LOG_TAG, error.message)
                        Logger.logErrorExtended(LOG_TAG, "Setup Storage Error\n$error")
                        return
                    }
                    Logger.logInfo(
                        LOG_TAG,
                        "Setting up storage symlinks at ~/storage/shared, ~/storage/downloads, ~/storage/dcim, ~/storage/pictures, ~/storage/music and ~/storage/movies for directories in \"" + Environment.getExternalStorageDirectory().absolutePath + "\"."
                    )
                    val sharedDir = Environment.getExternalStorageDirectory()
                    Os.symlink(sharedDir.absolutePath, File(storageDir, "shared").absolutePath)
                    val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    Os.symlink(
                        downloadsDir.absolutePath,
                        File(storageDir, "downloads").absolutePath
                    )
                    val dcimDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    Os.symlink(dcimDir.absolutePath, File(storageDir, "dcim").absolutePath)
                    val picturesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    Os.symlink(picturesDir.absolutePath, File(storageDir, "pictures").absolutePath)
                    val musicDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    Os.symlink(musicDir.absolutePath, File(storageDir, "music").absolutePath)
                    val moviesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    Os.symlink(moviesDir.absolutePath, File(storageDir, "movies").absolutePath)
                    val dirs = context.getExternalFilesDirs(null)
                    if (dirs != null && dirs.size > 1) {
                        for (i in 1 until dirs.size) {
                            val dir = dirs[i] ?: continue
                            val symlinkName = "external-$i"
                            Logger.logInfo(
                                LOG_TAG,
                                "Setting up storage symlinks at ~/storage/" + symlinkName + " for \"" + dir.absolutePath + "\"."
                            )
                            Os.symlink(dir.absolutePath, File(storageDir, symlinkName).absolutePath)
                        }
                    }
                    Logger.logInfo(LOG_TAG, "Storage symlinks created successfully.")
                } catch (e: Exception) {
                    Logger.logErrorAndShowToast(context, LOG_TAG, e.message)
                    Logger.logStackTraceWithMessage(
                        LOG_TAG,
                        "Setup Storage Error: Error setting up link",
                        e
                    )
                }
            }
        }.start()
    }

    private fun ensureDirectoryExists(directory: File): Error {
        return FileUtils.createDirectoryFile(directory.absolutePath)
    }

    private val zip: ByteArray
        external get
}