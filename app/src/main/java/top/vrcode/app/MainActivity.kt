package top.vrcode.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout
import android.os.Bundle
import android.content.res.Configuration
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.termux.shared.termux.TermuxUtils
import top.vrcode.app.components.TerminalDialog
import top.vrcode.app.errView.AddGraphicalSupportActivity
import top.vrcode.app.errView.TermuxNotEnableActivity
import top.vrcode.app.utils.Utils

class MainActivity : AppCompatActivity() {
    @JvmField
    var kbd: AdditionalKeyboardView? = null
    private var frm: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val termuxErr = TermuxUtils.isTermuxAppAccessible(applicationContext)
        if (termuxErr != null) {
            val intent = Intent(this, TermuxNotEnableActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        if (!Utils.checkGraphicalSupport()) {
            val intent = Intent(this, AddGraphicalSupportActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val inited = preferences.getBoolean(Constant.VRCODE_INITED_KEY, false)
        val desktopEnvType = preferences.getString(Constant.CHOSEN_DESKTOP_ENV_KEY, null)

        if (!inited || desktopEnvType == null) {
            setContentView(R.layout.main_activity)

            fun setInited() {
                preferences.edit().apply {
                    putBoolean(Constant.VRCODE_INITED_KEY, true)
                    apply()
                }
            }

            fun installChosenSoftware(deChosenID: Int, toolChosenIDs: MutableList<Int>) {
                preferences.edit().apply {
                    putString(
                        Constant.CHOSEN_DESKTOP_ENV_KEY,
                        Constant.AVAILABLE_DESKTOP_ENVS[deChosenID]
                    )
                    apply()
                }

                var execScript = ""
                execScript += Constant.DESKTOP_ENV_INSTALL_SCRIPTS[Constant.AVAILABLE_DESKTOP_ENVS[deChosenID]] + "\n"
                for (id in toolChosenIDs) {
                    val scriptOrName = Constant.TOOLS_INSTALL_SCRIPTS[Constant.AVAILABLE_TOOLS[id]]
                    if (scriptOrName != null) {
                        var realScript: String
                        if (scriptOrName.startsWith(Constant.SCRIPT_IN_ASSET)) {
                            realScript =
                                application.assets.open(scriptOrName.substring(Constant.SCRIPT_IN_ASSET.length))
                                    .bufferedReader().use {
                                        it.readText()
                                    }
                        } else {
                            realScript = scriptOrName
                        }
                        execScript += realScript + "\n"
                    }
                }
                val bashScript = Utils.BashScript(execScript)
                TerminalDialog(this)
                    .execute(
                        bashScript.get()
                    ).setPositiveButtonCallback { terminalDialog, terminalSession ->
                        run {
                            Log.d("TerminalCheck", terminalSession?.isRunning.toString())
                            if ((terminalSession?.isRunning != true)) {
                                terminalDialog.dismiss()
                                setInited()
                                Utils.reborn(application)
                            }
                        }
                    }.show(getString(R.string.run_install_chosen))
            }

            var deChosenID = 0;
            val toolChosenIDs = mutableListOf<Int>()

            val toolChooser = AlertDialog.Builder(this)
                .setTitle(getString(R.string.tools_chooser_dialog_title))
                .setMultiChoiceItems(Constant.AVAILABLE_TOOLS, null) { _, which, isChecked ->
                    run {
                        if (isChecked) {
                            toolChosenIDs.add(which)
                        } else {
                            toolChosenIDs.remove(which)
                        }
                    }
                }
                .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                    run {
                        dialog.dismiss()
                        installChosenSoftware(deChosenID, toolChosenIDs)
                    }
                }
                .create()
            toolChooser.setCanceledOnTouchOutside(false)

            val deChooser = AlertDialog.Builder(this)
                .setTitle(getString(R.string.de_chooser_dialog_title))
                .setSingleChoiceItems(
                    Constant.AVAILABLE_DESKTOP_ENVS,
                    deChosenID
                ) { _, which -> deChosenID = which }
                .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                    run {
                        dialog.dismiss()
                        toolChooser.show()
                    }
                }
                .create()
            deChooser.setCanceledOnTouchOutside(false)
            deChooser.show()
        } else {
            LorieService.setMainActivity(this)
            LorieService.start(LorieService.ACTION_START_FROM_ACTIVITY)
            window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            )
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.main_activity)

            kbd = findViewById(R.id.additionalKbd)
            frm = findViewById(R.id.frame)

            window.decorView.pointerIcon =
                PointerIcon.getSystemIcon(this, PointerIcon.TYPE_NULL)

            val desktopType = preferences.getString(Constant.CHOSEN_DESKTOP_ENV_KEY, null)
            val intent = Intent(this, DesktopEnvService::class.java)
            intent.putExtra(Constant.DESKTOP_TYPE_INTENT_KEY, desktopType)
            startService(intent)
//            val scriptString =
//                application.assets.open("xfce_session.sh")
//                    .bufferedReader().use {
//                        it.readText()
//                    }
//            val bashScript = Utils.BashScript(scriptString, true)
//            TerminalDialog(this)
//                .execute(
//                    bashScript.get()
//                ).setPositiveButtonCallback { terminalDialog, terminalSession ->
//                    run {
//                        Log.d("TerminalCheck", terminalSession?.isRunning.toString())
//                        if ((terminalSession?.isRunning != true)) {
//                            terminalDialog.dismiss()
//                        }
//                    }
//                }.show("Test")
        }
    }

    var orientation = 0
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation != orientation && kbd != null && kbd!!.visibility == View.VISIBLE) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            var view = currentFocus
            if (view == null) {
                view = View(this)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        orientation = newConfig.orientation
    }

    fun onLorieServiceStart(instance: LorieService) {
        val lorieView = findViewById<SurfaceView>(R.id.lorieView)
        instance.setListeners(lorieView)
        kbd?.reload(keys, lorieView, LorieService.getOnKeyListener())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val window = window
        val decorView = window.decorView
        if (preferences.getBoolean("Reseed", true)) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        } else {
            window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            )
        }
    }

    override fun onBackPressed() {}
//    public override fun onUserLeaveHint() {
//        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
//        if (preferences.getBoolean("PIP", true)) {
//            enterPictureInPictureMode()
//        }
//    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (kbd!!.visibility != View.VISIBLE) if (preferences.getBoolean(
                "showAdditionalKbd",
                true
            )
        ) {
            kbd!!.visibility = View.VISIBLE
            val paddingDp = 35
            val density = this.resources.displayMetrics.density
            val paddingPixel = (paddingDp * density).toInt()
            frm!!.setPadding(0, 0, 0, paddingPixel)
        }
        return

    }

    companion object {
        private val keys = intArrayOf(
            KeyEvent.KEYCODE_ESCAPE,
            KeyEvent.KEYCODE_TAB,
            KeyEvent.KEYCODE_CTRL_LEFT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT
        )
    }
}