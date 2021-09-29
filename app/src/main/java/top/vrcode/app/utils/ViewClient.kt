package top.vrcode.app.utils

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import java.lang.Exception

open class BasicSessionClient(var terminalView: TerminalView) : TerminalSessionClient{
    override fun onTextChanged(changedSession: TerminalSession?) {
        if (changedSession != null) {
            terminalView.onScreenUpdated()
        }
    }

    override fun onTitleChanged(changedSession: TerminalSession?) {
    }

    override fun onSessionFinished(finishedSession: TerminalSession?) {
    }

    override fun onCopyTextToClipboard(session: TerminalSession?, text: String?) {
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
    }

    override fun onBell(session: TerminalSession?) {
    }

    override fun onColorsChanged(session: TerminalSession?) {
        if (session != null) {
            terminalView.onScreenUpdated()
        }
    }

    override fun onTerminalCursorStateChange(state: Boolean) {
    }

    override fun getTerminalCursorStyle(): Int? {
        return null
    }

    override fun logError(tag: String?, message: String?) {
    }

    override fun logWarn(tag: String?, message: String?) {
    }

    override fun logInfo(tag: String?, message: String?) {
    }

    override fun logDebug(tag: String?, message: String?) {
    }

    override fun logVerbose(tag: String?, message: String?) {
    }

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
    }

    override fun logStackTrace(tag: String?, e: Exception?) {
    }
}

class BasicViewClient(val terminalView: TerminalView) : TerminalViewClient {
    override fun onScale(scale: Float): Float {
        if (scale < 0.9f || scale > 1.1f) {
            val increase = scale > 1f
            val changedSize = (if (increase) 1 else -1) * 2
            terminalView.setTextSize(40)
            return 1.0f
        }
        return scale
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        if (terminalView.isFocusable && terminalView.isFocusableInTouchMode) {
            (terminalView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(terminalView, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean {
        return false
    }

    override fun shouldEnforceCharBasedInput(): Boolean {
        return false
    }

    override fun shouldUseCtrlSpaceWorkaround(): Boolean {
        return false
    }

    override fun isTerminalViewSelected(): Boolean {
        return false
    }

    override fun copyModeChanged(copyMode: Boolean) {
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        return false
    }

    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
        return false
    }

    override fun readControlKey(): Boolean {
        return false
    }

    override fun readAltKey(): Boolean {
        return false
    }

    override fun readShiftKey(): Boolean {
        return false
    }

    override fun readFnKey(): Boolean {
        return false
    }

    override fun onCodePoint(
        codePoint: Int,
        ctrlDown: Boolean,
        session: TerminalSession?
    ): Boolean {
        return false
    }

    override fun onEmulatorSet() {
    }

    override fun logError(tag: String?, message: String?) {
    }

    override fun logWarn(tag: String?, message: String?) {
    }

    override fun logInfo(tag: String?, message: String?) {
    }

    override fun logDebug(tag: String?, message: String?) {
    }

    override fun logVerbose(tag: String?, message: String?) {
    }

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
    }

    override fun logStackTrace(tag: String?, e: Exception?) {
    }

    override fun onLongPress(event: MotionEvent?): Boolean {
        return false
    }
}