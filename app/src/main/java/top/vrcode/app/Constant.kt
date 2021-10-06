package top.vrcode.app

import com.termux.shared.termux.TermuxConstants
import java.io.File

object Constant {
    const val LATEST_MODIFIED_TERMUX_APP_URL =
        "https://github.com/VRCodeProject/termux-app/releases/download/20210922T145352/termux-app-arm64-v8a-release.apk" // FIXME: should update automatically
    const val LATEST_MODIFIED_TERMUX_APP_FILENAME = "termux-app-arm64-v8a-release.apk"

    const val VRCODE_FILE_PROVIDER_AUTHORITIES = "top.vrcode.app.fileprovider"

    const val VRCODE_INITED_KEY = "inited"
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
