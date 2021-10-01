package top.vrcode.app.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.termux.shared.models.ExecutionCommand
import com.termux.shared.shell.TermuxSession
import com.termux.shared.shell.TermuxShellEnvironmentClient
import com.termux.shared.terminal.TermuxTerminalSessionClientBase
import com.termux.shared.terminal.TermuxTerminalViewClientBase
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import top.vrcode.app.R
import java.lang.Exception

typealias DialogSessionCallback = (TerminalDialog, TerminalSession?) -> Unit

class TerminalDialog(val context: Context) {
    private val terminalWindowView = WindowTerminalView(context)

    private var dialog: AlertDialog? = null
    private var termuxSession: TermuxSession? = null
    private var terminalSession: TerminalSession? = null
    private var terminalSessionClient: TerminalSessionClient? = null
    private var sessionFinishedCallback: DialogSessionCallback? = null

    private var positiveButtonCallback: DialogSessionCallback? = null

    init {
        terminalWindowView.setTerminalViewClient(object : TermuxTerminalViewClientBase() {
            override fun logDebug(tag: String?, message: String?) {
                super.logDebug(tag, message)
                logIt(tag, message)
            }

            override fun logVerbose(tag: String?, message: String?) {
                super.logVerbose(tag, message)
                logIt(tag, message)
            }

            override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
                super.logStackTraceWithMessage(tag, message, e)
                logIt(tag, message, e)
            }

            override fun logStackTrace(tag: String?, e: Exception?) {
                super.logStackTrace(tag, e)
                logIt(tag, e.toString(), e)
            }

            override fun logInfo(tag: String?, message: String?) {
                super.logInfo(tag, message)
                logIt(tag, message)
            }

            override fun logError(tag: String?, message: String?) {
                super.logError(tag, message)
                logIt(tag, message)
            }

            override fun logWarn(tag: String?, message: String?) {
                super.logWarn(tag, message)
                logIt(tag, message)
            }

            fun logIt(tag: String?, message: String?, e: Exception? = null) {
                message?.let { Log.d("LogIt", it, e) }
            }
        })

        terminalSessionClient = object : TermuxTerminalSessionClientBase() {
            override fun onSessionFinished(finishedSession: TerminalSession?) {
                updateButtonVisible(View.VISIBLE)
                sessionFinishedCallback?.let {
                    it(this@TerminalDialog, finishedSession)
                }
                super.onSessionFinished(finishedSession)
            }

            override fun getTerminalCursorStyle(): Int {
                return TerminalEmulator.TERMINAL_CURSOR_STYLE_BAR
            }
        }
    }

    fun setPositiveButtonCallback(cb: DialogSessionCallback): TerminalDialog {
        positiveButtonCallback = cb
        return this
    }

    fun execute(command: ExecutionCommand): TerminalDialog {
        if (termuxSession != null) {
            termuxSession?.killIfExecuting(context, false)
        }

        dialog = AlertDialog.Builder(context)
            .setView(terminalWindowView.rootView)
            .setOnCancelListener {
                termuxSession?.killIfExecuting(context, false)
            }
            .setPositiveButton(context.getString(R.string.install_x_wayland_dialog_button)) { _, _ ->
                run {
                    positiveButtonCallback?.let {
                        it(this@TerminalDialog, termuxSession?.terminalSession)
                    }
                }
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
        terminalSession = termuxSession?.terminalSession
        terminalWindowView.terminalView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            run {
                terminalSession?.let {
                    terminalWindowView.attachSession(it)
                }
                terminalWindowView.setTerminalCursor(500) // Will return when view is null
            }
            // TODO: works as a trick, the view can only be measured after dialog exists. And getHeight && getWidth can work properly.
            // A better choice is define View draw ourselves.
        }
        return this
    }

    fun setTitle(title: String?): TerminalDialog {
        dialog?.setTitle(title)
        return this
    }

    fun onFinish(finishedCallback: DialogSessionCallback): TerminalDialog {
        this.sessionFinishedCallback = finishedCallback
        return this
    }

    fun show(title: String?) {
        dialog?.setTitle(title)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)

        updateButtonVisible(View.GONE)

        dialog?.show()
    }

    fun updateButtonVisible(visible: Int) {
        val button = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        button?.let {
            it.visibility = visible
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
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

    fun setTerminalCursor(blinkRate: Int) {
        terminalView.mEmulator.setCursorStyle()
        terminalView.setTerminalCursorBlinkerRate(blinkRate)
        terminalView.setTerminalCursorBlinkerState(true, false)
    }

    fun attachSession(terminalSession: TerminalSession?) {
        terminalView.attachSession(terminalSession)
    }

    fun setInputMethodEnabled(enabled: Boolean) {
        terminalView.isFocusable = enabled
        terminalView.isFocusableInTouchMode = enabled
    }
}