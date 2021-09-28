package top.vrcode.app.errView

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
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


class TermuxNotEnableActivity : AppCompatActivity() {
    var downloadManager: DownloadManager? = null
    var downloadId: Long? = null
    var downloadPath: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termux_not_enable)

        val installBtn = findViewById<Button>(R.id.install_termux)
        val installTitle = findViewById<AppCompatTextView>(R.id.install_termux_title)

        val err = TermuxUtils.isTermuxAppInstalled(applicationContext)

        if (err == null) {
            installTitle.text = getString(R.string.install_official_termux)
        }

        installBtn.setOnClickListener {
            installTermuxFromNetwork()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkStatus()
        }
    }

    @SuppressLint("Range")
    private fun checkStatus() {
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
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    installApk()
                    cursor.close()
                }
                DownloadManager.STATUS_FAILED -> {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.download_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    cursor.close()
                    applicationContext.unregisterReceiver(receiver)
                }
            }
        }
    }

    private fun installTermuxFromNetwork() {
        val uri = Uri.parse(Constant.LATEST_MODIFIED_TERMUX_APP_URL)
        val file = File(applicationContext.externalCacheDir, Constant.LATEST_MODIFIED_TERMUX_APP_FILENAME)
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
    }

    private fun setPermission(absolutePath: String) {
        val command = "chmod 777 $absolutePath"
        val runtime = Runtime.getRuntime()
        try {
            runtime.exec(command)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun installApk(file: File = downloadPath!!) {
        setPermission(file.absolutePath)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val apkUri =
            FileProvider.getUriForFile(this, Constant.VRCODE_FILE_PROVIDER_AUTHORITIES, file)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        applicationContext.startActivity(intent)
    }
}