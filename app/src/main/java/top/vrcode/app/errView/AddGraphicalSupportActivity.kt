package top.vrcode.app.errView

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.termux.shared.models.ExecutionCommand
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import top.vrcode.app.R
import top.vrcode.app.utils.BasicSessionClient
import top.vrcode.app.utils.BasicViewClient

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
    private val terminalWindowView = WindowTerminalView(context)

    private var dialog: AlertDialog? = null
    private var terminalSession: TerminalSession? = null
    private var terminalSessionClient: TerminalSessionClient? = null
    private var sessionFinishedCallback: DialogSessionFinished? = null

    private var cancelListener: DialogInterface.OnCancelListener? = null

    init {
        terminalWindowView.setTerminalViewClient(BasicViewClient(terminalWindowView.terminalView))
        terminalSessionClient = object : BasicSessionClient(terminalWindowView.terminalView) {
            override fun onSessionFinished(finishedSession: TerminalSession?) {
                sessionFinishedCallback?.let {
                    it(this@TerminalDialog, finishedSession)
                }
                super.onSessionFinished(finishedSession)
            }
        }
    }

    fun execute(executablePath: String, arguments: Array<String>?): TerminalDialog {
        if (terminalSession != null) {
            terminalSession?.finishIfRunning()
        }

        dialog = AlertDialog.Builder(context)
            .setView(terminalWindowView.rootView)
            .setOnCancelListener {
                terminalSession?.finishIfRunning()
                cancelListener?.onCancel(it)
            }
            .create()

//        val session =
        return this
    }

}

class WindowTerminalView(val context: Context) {
    @SuppressLint("InflateParams")
    var rootView: View =
        LayoutInflater.from(context).inflate(R.layout.dialog_terminal_view, null, false)
        private set

    var terminalView: TerminalView = rootView.findViewById(R.id.terminal_dialog)
        private set

    init {
        terminalView.setTextSize(30)
        terminalView.setTypeface(Typeface.MONOSPACE)
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