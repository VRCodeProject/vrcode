package top.vrcode.app

object Constant {
    const val LATEST_MODIFIED_TERMUX_APP_URL =
        "https://github.com/VRCodeProject/termux-app/releases/download/20210922T145352/termux-app-arm64-v8a-release.apk" // FIXME: should update automatically
    const val LATEST_MODIFIED_TERMUX_APP_FILENAME = "termux-app-arm64-v8a-release.apk"

    const val VRCODE_FILE_PROVIDER_AUTHORITIES = "top.vrcode.app.fileprovider"

    const val VRCODE_INITED_KEY = "inited"

    const val X11_INSTALL_SCRIPT =
        "pkg install -y x11-repo -y && apt --yes --force-yes update && apt --yes --force-yes install xwayland"

    const val SCRIPT_IN_ASSET = "assets/" // If script is located in assets

    // Desktop environment vars
    val DESKTOP_ENV_INSTALL_SCRIPTS = mapOf(
        "Xfce" to "apt --yes --force-yes install xfce"
    )
    val DESKTOP_ENV_STARTUP_SCRIPTS = mapOf(
        "Xfce" to "xfce_session.sh"
    )
    val AVAILABLE_DESKTOP_ENVS = arrayOf("Xfce")

    const val CHOSEN_DESKTOP_ENV_KEY = "desktopEnv"

    val TOOLS_INSTALL_SCRIPTS = mapOf<String, String>(
        "VS Code" to "${SCRIPT_IN_ASSET}vscode.sh",
        "Golang" to "pkg install golang -y"
    )
    val AVAILABLE_TOOLS = TOOLS_INSTALL_SCRIPTS.keys.toTypedArray()

    const val XFCE_SESSION_SCRIPT = "xfce_session.sh"

    const val DESKTOP_SESSION_NOTIFICATION_KEY = "desktop_id"
    const val DESKTOP_SESSION_NOTIFICATION_ID = 2021

    const val DESKTOP_TYPE_INTENT_KEY = "desktop_type"
}
