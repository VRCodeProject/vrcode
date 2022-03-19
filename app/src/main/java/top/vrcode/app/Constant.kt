package top.vrcode.app

import com.termux.shared.termux.TermuxConstants
import java.io.File

object Constant {
    const val TERMUX_PLUGIN_FILENAME = "plugin.apk"
    const val TERMUX_BOOTSTRAP_FILENAME = "bootstrap-aarch64.zip"

    const val VRCODE_FILE_PROVIDER_AUTHORITIES = "top.vrcode.app.fileprovider"

    val VRCODE_INIT_FILE = File("${TermuxConstants.TERMUX_ETC_PREFIX_DIR_PATH}/vrcode.inited")

    const val X11_INSTALL_SCRIPT =
        "pkg install -y x11-repo -y && apt --yes --force-yes update && apt --yes --force-yes install xwayland"

    // Desktop environment vars
    const val LINUX_ENV_INSTALL_SCRIPT = "linux_install.sh"
    const val LINUX_ENV_INTERNAL_INSTALL_SCRIPT = "linux_internal_install.sh"
    const val LINUX_ENV_STARTUP_SCRIPT = "linux_startup.sh"

    const val LINUX_INSTALL_SCRIPT_INTERNAL_SCRIPT_PLACEHOLDER = "SCRIPT_PLACEHOLDER"

    val XWAYLAND_ENVS =
        arrayOf("XDG_RUNTIME_DIR=/data/data/com.termux/files/usr/tmp")
}
