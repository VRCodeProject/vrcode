package top.vrcode.app.errView

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import top.vrcode.app.R

// https://github.com/NeoTerm/NeoTerm/blob/236072395ce056d2d2cccf950d3f243f099a178f/app/src/main/java/io/neoterm/frontend/floating/dialog.kt
class AddGraphicalSupportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_graphical_support)

        val installBtn = findViewById<Button>(R.id.install_x_wayland)
        installBtn.setOnClickListener { installXWayland() }
    }

    fun installXWayland() {

    }
}

typealias DialogSessionFinished = (TerminalDialog, TerminalSession?) -> Unit

class TerminalDialog(val context: Context) {
    private val termWindowView = WindowTerminalView(context)
    private val terminalSessionCallback: BasicSessionCallback
    private var dialog: AlertDialog? = null
    private var terminalSession: TerminalSession? = null
    private var sessionFinishedCallback: DialogSessionFinished? = null
    private var cancelListener: DialogInterface.OnCancelListener? = null
}

class WindowTerminalView(val context: Context) {
    @SuppressLint("InflateParams")
    var rootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_terminal_view,null,false)
    private set

    var terminalView: TerminalView = rootView.findViewById(R.id.terminal_dialog)
    private set

    init {
        terminalView.setTextSize(30)
    }

    fun setTerminalViewClient(terminalViewClient: TerminalViewClient?) {
        terminalView.setTerminalViewClient(terminalViewClient)
    }

    fun attachSession(terminalSession: TerminalSession?) {
        terminalView.attachSession(terminalSession)
    }

    fun setInputMethodEnabled(enabled: Boolean) {
        terminalView.isFocusable = enabled
        terminalView.isFocusableInTouchMode = enabled
    }
}