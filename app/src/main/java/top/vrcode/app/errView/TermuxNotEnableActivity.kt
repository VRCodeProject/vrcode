package top.vrcode.app.errView

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

class TermuxNotEnableActivity : AppCompatActivity() {
    var downloadManager: DownloadManager? = null
    var downloadId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termux_not_enable)

        val installBtn = findViewById<Button>(R.id.install_termux)
        val installTitle = findViewById<AppCompatTextView>(R.id.install_termux_title)

        val err = TermuxUtils.isTermuxAppInstalled(applicationContext)

        if (err != null) {
            installTitle.text = getString(R.string.install_official_termux)
        }

        installBtn.setOnClickListener {

        }
    }

    fun installTermuxFromNetwork() {
        val uri = Uri.parse(Constant.LATEST_MODIFIED_TERMUX_APP_URL)
        val file = File(applicationContext.cacheDir, Constant.LATEST_MODIFIED_TERMUX_APP_FILENAME)
        val request = DownloadManager.Request(uri).apply {
            setAllowedOverRoaming(true)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            setDestinationUri(Uri.fromFile(file))
        }
        val pathStr = file.absoluteFile

        if (downloadManager == null) {
            downloadManager =
                applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        }

        downloadManager!!.enqueue(request)

    }
}