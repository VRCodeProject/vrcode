package top.vrcode.app.errView

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.FileProvider
import com.termux.shared.termux.TermuxUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink
import top.vrcode.app.Constant
import top.vrcode.app.R
import top.vrcode.app.utils.Utils
import top.vrcode.app.utils.Utils.setPermission
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread


class TermuxNotEnableActivity : AppCompatActivity() {

    private var progressDialog: ProgressDialog? = null
    var mContext: Context? = null
    var apkFile: File? = null
    var mDownloadFlag = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termux_not_enable)
        mContext = this
        apkFile =
            File(applicationContext.externalCacheDir, Constant.LATEST_MODIFIED_TERMUX_APP_FILENAME)

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
                    mDownloadFlag = true
                    installApk(apkFile!!)
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

        if (mDownloadFlag) {
            installApk(apkFile!!)
            return
        }

        if (apkFile!!.exists()) {
            apkFile!!.delete()
        } // prevent duplicate download

        thread {
            download(uri, apkFile!!)
        }
    }

    private fun installApk(file: File) {
        setPermission(file.absolutePath)
        val intent = Intent(Intent.ACTION_VIEW)
        val apkUri =
            FileProvider.getUriForFile(this, Constant.VRCODE_FILE_PROVIDER_AUTHORITIES, file)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        mContext!!.startActivity(intent)
    }
}