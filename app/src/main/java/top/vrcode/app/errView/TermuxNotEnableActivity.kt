package top.vrcode.app.errView

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.FileProvider
import com.termux.shared.termux.TermuxUtils
import top.vrcode.app.Constant
import top.vrcode.app.R
import top.vrcode.app.utils.TermuxInstaller
import top.vrcode.app.utils.Utils
import top.vrcode.app.utils.Utils.setPermission
import java.io.File
import java.io.InputStream
import java.io.OutputStream


class TermuxNotEnableActivity : AppCompatActivity() {

    var mContext: Context? = null
    var apkFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termux_not_enable)
        mContext = this
        apkFile =
            File(applicationContext.externalCacheDir, Constant.TERMUX_PLUGIN_FILENAME)

        val installBtn = findViewById<AppCompatButton>(R.id.install_termux)
        val goMainBtn = findViewById<AppCompatButton>(R.id.termux_install2main)
        val installTitle = findViewById<AppCompatTextView>(R.id.install_termux_title)

        val err = TermuxUtils.isTermuxAppInstalled(applicationContext)

        if (err == null) {
            installTitle.text = getString(R.string.install_official_termux)
        }

        installBtn.setOnClickListener {
            installTermuxPlugin()
        }

        goMainBtn.setOnClickListener {
            if (TermuxUtils.isTermuxAppInstalled(applicationContext) == null &&
                TermuxUtils.getTermuxPackageContext(this) != null
            ) {
                TermuxInstaller.setupBootstrapIfNeeded(this) { Utils.reborn(application) }
            } else {
                Toast.makeText(this, "com.termux not accessible.", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun installTermuxPlugin() {
        fun copy(input: InputStream, output: OutputStream) {
            val buffer = ByteArray(1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            input.close()
            output.close()
        }

        val asset = assets.open(Constant.TERMUX_PLUGIN_FILENAME)
        val outStream = apkFile?.outputStream()
        copy(asset, outStream!!)

        installApk(apkFile!!)
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