package top.vrcode.app.errView

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.termux.shared.models.ExecutionCommand
import top.vrcode.app.R
import top.vrcode.app.components.TerminalDialog
import top.vrcode.app.utils.Utils

// https://github.com/NeoTerm/NeoTerm/blob/236072395ce056d2d2cccf950d3f243f099a178f/app/src/main/java/io/neoterm/frontend/floating/dialog.kt
class AddGraphicalSupportActivity : AppCompatActivity() {
    @Suppress("PrivatePropertyName")
    private var COMMAND_ID = 2003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_graphical_support)

        val installBtn = findViewById<Button>(R.id.install_x_wayland)
        installBtn.setOnClickListener { installXWayland() }
    }

    private fun getNextCommandID() = COMMAND_ID++

    private fun installXWayland() {
        TerminalDialog(this)
            .execute(
                ExecutionCommand(
                    getNextCommandID(),
                    "bash",
                    Utils.script2BashArray("pkg install x11-repo -y && pkg update -y && pkg install xwayland -y"),
                    null,
                    null,
                    false,
                    false
                )
            ).show("Installing XWayland")
    }
}