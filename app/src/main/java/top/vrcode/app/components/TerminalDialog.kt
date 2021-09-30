package top.vrcode.app.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.termux.shared.models.ExecutionCommand
import com.termux.shared.shell.TermuxSession
import com.termux.shared.shell.TermuxShellEnvironmentClient
import com.termux.shared.terminal.TermuxTerminalSessionClientBase
import com.termux.shared.terminal.TermuxTerminalViewClientBase
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import top.vrcode.app.R

typealias DialogSessionFinished = (TerminalDialog, TerminalSession?) -> Unit

class TerminalDialog(val context: Context) {
    private val terminalWindowView = WindowTerminalView(context)

    private var dialog: AlertDialog? = null
    private var termuxSession: TermuxSession? = null
    private var terminalSessionClient: TerminalSessionClient? = null
    private var sessionFinishedCallback: DialogSessionFinished? = null

    private var cancelListener: DialogInterface.OnCancelListener? = null

    init {
        terminalWindowView.setTerminalViewClient(TermuxTerminalViewClientBase())
        terminalWindowView.setTerminalCursorBlinkerRate(1000)
        terminalSessionClient = object : TermuxTerminalSessionClientBase() {
            override fun onSessionFinished(finishedSession: TerminalSession?) {
                sessionFinishedCallback?.let {
                    it(this@TerminalDialog, finishedSession)
                }
                super.onSessionFinished(finishedSession)
            }
        }


    }

    fun execute(command: ExecutionCommand): TerminalDialog {
        if (termuxSession != null) {
            termuxSession?.killIfExecuting(context, false)
        }

        dialog = AlertDialog.Builder(context)
            .setView(terminalWindowView.rootView)
            .setOnCancelListener {
                termuxSession?.killIfExecuting(context, false)
                cancelListener?.onCancel(it)
            }
            .create()

//        terminalSession = TermuxSession.execute(this, command, terminalSessionClient, null, TermuxShellEnvironmentClient(), true)
        termuxSession = TermuxSession.execute(
            context,
            command,
            terminalSessionClient!!,
            null,
            TermuxShellEnvironmentClient(),
            "wayland",
            true
        )
        val terminalSession = termuxSession?.terminalSession

        terminalSession?.let {
            terminalWindowView.attachSession(it)
        }

        return this
    }

    fun onDismiss(cancelListener: DialogInterface.OnCancelListener?): TerminalDialog {
        this.cancelListener = cancelListener
        return this
    }

    fun setTitle(title: String?): TerminalDialog {
        dialog?.setTitle(title)
        return this
    }

    fun onFinish(finishedCallback: DialogSessionFinished): TerminalDialog {
        this.sessionFinishedCallback = finishedCallback
        return this
    }

    fun show(title: String?) {
        dialog?.setTitle(title)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        dialog?.show()
    }

    fun dismiss(): TerminalDialog {
        dialog?.dismiss()
        return this
    }

    fun imeEnabled(enabled: Boolean): TerminalDialog {
        if (enabled) {
            terminalWindowView.setInputMethodEnabled(true)
        }
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
        terminalView.setTextSize(40)
        terminalView.setTypeface(Typeface.MONOSPACE)
    }


    fun setTerminalViewClient(terminalViewClient: TerminalViewClient?) {
        terminalView.setTerminalViewClient(terminalViewClient)
    }

    fun setTerminalCursorBlinkerRate(blinkRate: Int) {
        terminalView.setTerminalCursorBlinkerRate(blinkRate)
        terminalView.setTerminalCursorBlinkerState(true, true)
    }

    fun attachSession(terminalSession: TerminalSession?) {
        terminalView.attachSession(terminalSession)
    }

    fun setInputMethodEnabled(enabled: Boolean) {
        terminalView.isFocusable = enabled
        terminalView.isFocusableInTouchMode = enabled
    }
}