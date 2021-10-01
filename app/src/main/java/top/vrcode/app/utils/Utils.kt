package top.vrcode.app.utils

import com.termux.shared.models.ExecutionCommand
import com.termux.shared.termux.TermuxConstants
import java.io.File
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

object Utils {

    private fun checkXWaylandInstallation(): Boolean {
        val xWaylandFilePath = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/Xwayland"
        val xWaylandFile = File(xWaylandFilePath)
        return xWaylandFile.exists()
    }

    fun checkGraphicalSupport(): Boolean {
        return checkXWaylandInstallation()
    }

    fun md5(str: String): ByteArray =
        MessageDigest.getInstance("MD5").digest(str.toByteArray(UTF_8))

    fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

    class BashScript(val command: String) {
        companion object {
            @JvmStatic
            private var COMMAND_ID = 2003
        }

        var executionCommand: ExecutionCommand
        lateinit var file: File
        lateinit var filename: String


        init {
            executionCommand = ExecutionCommand(
                getNextCommandID(),
                "bash",
                saveTempScript(command),
                null,
                null,
                false,
                false
            )
        }

        private fun getNextCommandID() = COMMAND_ID++

        private fun saveTempScript(s: String): Array<String> {
            filename = "${TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH}/${md5(s).toHex()}.sh"
            file = File(filename)

            if (!file.exists()) {
                file.writeText(s)
            }


            return arrayOf(file.absolutePath)
        }

        fun get(): ExecutionCommand {
            return executionCommand
        }

        fun destroy() {
            file.delete()
        }


    }
}