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


class TermuxNotEnableActivity : AppCompatActivity() {
    private var downloadManager: DownloadManager? = null
    private var downloadId: Long? = null
    private var downloadPath: File? = null

    private var progressDialog: ProgressDialog? = null

    private val mExecutor = Executors.newSingleThreadScheduledExecutor()
    private var mFuture: ScheduledFuture<*>? = null


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

        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationContext.unregisterReceiver(downloadReceiver)
    }

    private val downloadReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkStatus()
        }
    }

    @SuppressLint("Range")
    private fun checkStatus() {
        Log.d("CheckStatus", "Being Notified")
        val query = DownloadManager.Query()
        query.setFilterById(downloadId!!)
        val cursor: Cursor = downloadManager!!.query(query)
        if (cursor.moveToFirst()) {
            when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_PAUSED -> {
                }
                DownloadManager.STATUS_PENDING -> {
                }
                DownloadManager.STATUS_RUNNING -> {
                    val soFarSize: Long =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val totalSize: Long =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val downloadProgress = (soFarSize * 1.0f / totalSize * 100).toInt()
                    Log.d("Running Report", "Report $downloadProgress")
                    progressDialog!!.progress = downloadProgress
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    mFuture?.run {
                        if (!isCancelled) cancel(true)
                    }
                    progressDialog!!.dismiss()
                    cursor.close()
                    Handler(Looper.getMainLooper()).postDelayed(
                        { installApk() }, 500)

                }
                DownloadManager.STATUS_FAILED -> {
                    mFuture?.run {
                        if (!isCancelled) cancel(true)
                    }
                    progressDialog!!.dismiss()
                    cursor.close()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.download_failed),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
    }

    private fun installTermuxFromNetwork() {
        val uri = Uri.parse(Constant.LATEST_MODIFIED_TERMUX_APP_URL)
        val file =
            File(applicationContext.externalCacheDir, Constant.LATEST_MODIFIED_TERMUX_APP_FILENAME)

        if (file.exists()) {
            file.delete()
        } // prevent duplicate download

        val request = DownloadManager.Request(uri).apply {
            setAllowedOverRoaming(true)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setDestinationUri(Uri.fromFile(file))
        }
        downloadPath = file.absoluteFile

        if (downloadManager == null) {
            downloadManager =
                applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        }

        downloadId = downloadManager!!.enqueue(request)

        mFuture = mExecutor.scheduleAtFixedRate({
            checkStatus()
        }, 300, 300, TimeUnit.MILLISECONDS)
    }


    private fun installApk(file: File = downloadPath!!) {
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