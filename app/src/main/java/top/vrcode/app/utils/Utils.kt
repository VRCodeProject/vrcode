package top.vrcode.app.utils

import com.termux.shared.termux.TermuxConstants
import java.io.File

object Utils {
    fun checkX11RepoInstallation(): Boolean {
        val x11FilePath =
            TermuxConstants.TERMUX_ETC_PREFIX_DIR_PATH + "/apt/sources.list.d/x11.list"
        val x11File = File(x11FilePath)
        return x11File.exists()
    }

    fun checkXWaylandInstallation(): Boolean {
        val xWaylandFilePath = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/XWayland"
        val xWaylandFile = File(xWaylandFilePath)
        return xWaylandFile.exists()
    }

    fun checkGraphicalSupport(): Boolean {
        return checkX11RepoInstallation() and checkXWaylandInstallation()
    }

    fun script2BashArray(command: String): Array<String> {
        return arrayOf("eval", "\"$command\"")
    }
}