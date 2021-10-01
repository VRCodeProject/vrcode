package top.vrcode.app

object Constant {
    const val LATEST_MODIFIED_TERMUX_APP_URL =
        "https://github.com/VRCodeProject/termux-app/releases/download/20210922T145352/termux-app-arm64-v8a-release.apk" // FIXME: should update automatically
    const val LATEST_MODIFIED_TERMUX_APP_FILENAME = "termux-app-arm64-v8a-release.apk"

    const val VRCODE_FILE_PROVIDER_AUTHORITIES = "top.vrcode.app.fileprovider"

    const val VRCODE_INITED_KEY = "inited"


    @Suppress("SpellCheckingInspection")
    val AVAILABLE_DESKTOP_ENVS = arrayOf("Xfce")
}
