package top.vrcode.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.termux.shared.termux.TermuxUtils
import top.vrcode.app.components.TerminalDialog
import top.vrcode.app.errView.AddGraphicalSupportActivity
import top.vrcode.app.errView.TermuxNotEnableActivity
import top.vrcode.app.utils.Utils

class InitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        val termuxErr = TermuxUtils.isTermuxAppAccessible(applicationContext)
        if (termuxErr != null) {
            Log.e("TermuxCheck", termuxErr)
            val intent = Intent(this, TermuxNotEnableActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        if (!Utils.checkGraphicalSupport()) {
            val intent = Intent(this, AddGraphicalSupportActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        fun setInited() {
            Constant.VRCODE_INIT_FILE.writeText("true")
        }

        fun setupLinux() {
            var installScriptString =
                Utils.getAssetScript(Constant.LINUX_ENV_INSTALL_SCRIPT, application)
            val internalInstallScriptString =
                Utils.getAssetScript(Constant.LINUX_ENV_INTERNAL_INSTALL_SCRIPT, application)

            val internalInstallScript = Utils.BashScript(internalInstallScriptString)
            installScriptString = installScriptString.replace(
                Constant.LINUX_INSTALL_SCRIPT_INTERNAL_SCRIPT_PLACEHOLDER,
                internalInstallScript.plainFilename
            )
            val installScript = Utils.BashScript(installScriptString)
            TerminalDialog(this)
                .execute(
                    installScript.get()
                ).setPositiveButtonCallback { terminalDialog, terminalSession ->
                    run {
                        Log.d("TerminalCheck", terminalSession?.isRunning.toString())
                        if ((terminalSession?.isRunning != true)) {
                            terminalDialog.dismiss()
                            setInited()
                            finish()
                            Utils.reborn(application)
                        }
                    }
                }.show(getString(R.string.install_linux_dialog_title))
        }
        if (!Constant.VRCODE_INIT_FILE.exists()) {
            setupLinux()
        }

        startActivity(Intent(this, MainActivity::class.java))
    }
}