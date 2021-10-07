package top.vrcode.app.errView

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.termux.shared.termux.TermuxUtils
import top.vrcode.app.Constant
import top.vrcode.app.R
import java.io.File
import java.io.IOException
import android.content.Intent
import android.database.Cursor
import androidx.core.content.FileProvider
import android.widget.Toast
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import top.vrcode.app.utils.Utils
import top.vrcode.app.utils.Utils.setPermission
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

import androidx.annotation.NonNull
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.*
import java.lang.Exception
import kotlin.concurrent.thread


class TermuxNotEnableActivity : AppCompatActivity() {

    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termux_not_enable)

        val installBtn = findViewById<AppCompatButton>(R.id.install_termux)
        val goMainBtn = findViewById<AppCompatButton>(R.id.termux_install2main)
        val installTitle = findViewById<AppCompatTextView>(R.id.install_termux_title)

        val err = TermuxUtils.isTermuxAppInstalled(applicationContext)

        if (err == null) {
            installTitle.text = getString(R.string.install_official_termux)
        }

        progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.downloading_termux))
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

        installBtn.setOnClickListener {
            progressDialog!!.show()
            installTermuxFromNetwork()
        }

        goMainBtn.setOnClickListener {
            Utils.reborn(application)
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
        }

//        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onDestroy() {
        super.onDestroy()
//        applicationContext.unregisterReceiver(downloadReceiver)
    }

//    private val downloadReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            checkStatus()
//        }
//    }
//
//    @SuppressLint("Range")
//    private fun checkStatus() {
//        Log.d("CheckStatus", "Being Notified")
//        val query = DownloadManager.Query()
//        query.setFilterById(downloadId!!)
//        val cursor: Cursor = downloadManager!!.query(query)
//        if (cursor.moveToFirst()) {
//            when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
//                DownloadManager.STATUS_PAUSED -> {
//                }
//                DownloadManager.STATUS_PENDING -> {
//                }
//                DownloadManager.STATUS_RUNNING -> {
//                    val soFarSize: Long =
//                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
//                    val totalSize: Long =
//                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
//                    val downloadProgress = (soFarSize * 1.0f / totalSize * 100).toInt()
//                    Log.d("Running Report", "Report $downloadProgress")
//                    progressDialog!!.progress = downloadProgress
//                }
//                DownloadManager.STATUS_SUCCESSFUL -> {
//                    mFuture?.run {
//                        if (!isCancelled) cancel(true)
//                    }
//                    progressDialog!!.dismiss()
//                    cursor.close()
//                    Handler(Looper.getMainLooper()).postDelayed(
//                        { installApk() }, 500)
//
//                }
//                DownloadManager.STATUS_FAILED -> {
//                    mFuture?.run {
//                        if (!isCancelled) cancel(true)
//                    }
//                    progressDialog!!.dismiss()
//                    cursor.close()
//                    Toast.makeText(
//                        applicationContext,
//                        getString(R.string.download_failed),
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                }
//            }
//        }
//    }

    //    private fun download(url: String, destFile: File) {
//        val request: Request = HttpUrl.Builder().url(url).build()
//        val response: Response = okHttpClient.newCall(request).execute()
//        val body = response.body
//        val contentLength = body!!.contentLength()
//        val source = body.source()
//        val sink: BufferedSink = Okio.buffer(Okio.sink(destFile))
//        val sinkBuffer: Buffer = sink.buffer()
//        var totalBytesRead: Long = 0
//        val bufferSize = 8 * 1024
//        var bytesRead: Long
//        while (source.read(sinkBuffer, bufferSize.toLong()).also { bytesRead = it } != -1L) {
//            sink.emit()
//            totalBytesRead += bytesRead
//            val progress = (totalBytesRead * 100 / contentLength).toInt()
//            publishProgress(progress)
//        }
//        sink.flush()
//        sink.close()
//        source.close()
//    }
    @Suppress("SameParameterValue")
    private fun download(url: String, file: File) {
        val request = Request.Builder().url(url).build()
        lateinit var response: Response
        try {
            response = OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
            return
        }
        val body = response.body
        val contentLength = body!!.contentLength()
        val source = body.source()
        val sink = file.sink().buffer()
        val sinkBuffer = sink.buffer
        var totalBytesRead: Long = 0
        val bufferSize = 8 * 1024
        var currentBytesRead: Long
        try {
            while (source.read(sinkBuffer, bufferSize.toLong())
                    .also { currentBytesRead = it } != -1L
            ) {
                sink.emit()
                totalBytesRead += currentBytesRead
                val progress = (totalBytesRead * 1.0f / contentLength * 100).toInt()
                Log.d("Progress", "${totalBytesRead}:$contentLength")
                runOnUiThread {
                    progressDialog!!.progress = progress
                }
            }
            throw IOException("Finish")
        } catch (e: IOException) {
            sink.writeAll(source);
            Log.d("Result", "Catch IOError")
            runOnUiThread {
                progressDialog!!.dismiss()
                Log.d("Test", "${file.length()}:${contentLength}")
                if (file.length() == contentLength) {
                    installApk(file)
                } else {
                    e.printStackTrace()
                    Toast.makeText(this, getString(R.string.download_failed), Toast.LENGTH_LONG)
                        .show()
                }
            }

        } catch (e: Exception) {
            runOnUiThread {
                progressDialog!!.dismiss()
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        } finally {
            sink.flush()
            sink.close()
            source.close()
        }
    }

    private fun installTermuxFromNetwork() {
        val uri = Constant.LATEST_MODIFIED_TERMUX_APP_URL
        val file =
            File(applicationContext.externalCacheDir, Constant.LATEST_MODIFIED_TERMUX_APP_FILENAME)

        if (file.exists()) {
            file.delete()
        } // prevent duplicate download

        thread {
            download(uri, file)
        }
    }


    private fun installApk(file: File) {
        setPermission(file.absolutePath)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val apkUri =
            FileProvider.getUriForFile(this, Constant.VRCODE_FILE_PROVIDER_AUTHORITIES, file)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        applicationContext.startActivity(intent)
    }
}