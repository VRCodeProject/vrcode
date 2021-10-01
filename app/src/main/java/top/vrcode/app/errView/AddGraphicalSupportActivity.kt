package top.vrcode.app.errView

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import top.vrcode.app.MainActivity
import top.vrcode.app.R
import top.vrcode.app.components.TerminalDialog
import top.vrcode.app.utils.Utils

// https://github.com/NeoTerm/NeoTerm/blob/236072395ce056d2d2cccf950d3f243f099a178f/app/src/main/java/io/neoterm/frontend/floating/dialog.kt
class AddGraphicalSupportActivity : AppCompatActivity() {
    @Suppress("PrivatePropertyName")

    lateinit var bashScript: Utils.BashScript

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_graphical_support)

        bashScript =
            Utils.BashScript("apt --yes --force-yes install x11-repo -y && apt --yes --force-yes update && apt --yes --force-yes install xwayland")

        val installBtn = findViewById<Button>(R.id.install_x_wayland)
        installBtn.setOnClickListener { installXWayland() }
    }

    override fun onDestroy() {
        super.onDestroy()
        bashScript.destroy()
    }


    private fun installXWayland() {
        TerminalDialog(this)
            .execute(
                bashScript.get()
            ).setPositiveButtonCallback { terminalDialog, terminalSession ->
                run {
                    Log.d("TerminalCheck", terminalSession?.isRunning.toString())
                    if (terminalSession?.isRunning != true) {
                        terminalDialog.dismiss()
                        this@AddGraphicalSupportActivity.startActivity(
                            Intent(
                                this@AddGraphicalSupportActivity,
                                MainActivity::class.java
                            )
                        )
                    }
                }
            }
            .show("Installing XWayland")
    }
}