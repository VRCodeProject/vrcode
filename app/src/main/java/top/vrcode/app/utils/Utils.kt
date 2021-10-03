package top.vrcode.app.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.termux.shared.models.ExecutionCommand
import com.termux.shared.termux.TermuxConstants
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

object Utils {

    private fun checkXWaylandInstallation(): Boolean {
        val xWaylandFilePath = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/Xwayland"
        val xWaylandFile = File(xWaylandFilePath)
        return xWaylandFile.exists()
    }

    fun reborn(application: Application) {
        Handler().postDelayed({
            val launchIntent =
                application.applicationContext.packageManager.getLaunchIntentForPackage(application.packageName)
            launchIntent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            application.applicationContext.startActivity(launchIntent)
        }, 100)
    }

    fun getAssetScript(name: String, application: Application): String {
        val script = application.assets.open(name).bufferedReader().use {
            it.readText()
        }
        return script
    }

    fun checkGraphicalSupport(): Boolean {
        return checkXWaylandInstallation()
    }

    fun setPermission(absolutePath: String) {
        val command = "chmod 777 $absolutePath"
        val runtime = Runtime.getRuntime()
        try {
            runtime.exec(command)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun md5(str: String): ByteArray =
        MessageDigest.getInstance("MD5").digest(str.toByteArray(UTF_8))

    fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

    class BashScript(val command: String, background: Boolean = false) {
        companion object {
            @JvmStatic
            private var COMMAND_ID = 2003
        }

        var executionCommand: ExecutionCommand
        lateinit var file: File
        lateinit var filename: String
        lateinit var plainFilename: String


        init {
            executionCommand = ExecutionCommand(
                getNextCommandID(),
                "bash",
                saveTempScript(command),
                null,
                null,
                background,
                false
            )
        }

        private fun getNextCommandID() = COMMAND_ID++

        private fun saveTempScript(s: String): Array<String> {
            plainFilename = "${md5(s).toHex()}.sh"
            filename = "${TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH}/$plainFilename"

            file = File(filename)

            if (!file.exists()) {
                file.writeText(s)
            }


            return arrayOf(file.absolutePath)
        }

        fun get(): ExecutionCommand {
            return executionCommand
        }

        fun destroy() {
            file.delete()
        }


    }
}