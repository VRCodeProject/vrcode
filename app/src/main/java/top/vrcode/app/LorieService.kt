package top.vrcode.app

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.termux.shared.shell.TermuxSession
import com.termux.shared.shell.TermuxShellEnvironmentClient
import com.termux.shared.terminal.TermuxTerminalSessionClientBase
import com.termux.terminal.TerminalSession
import top.vrcode.app.TouchParser.OnTouchParseListener
import top.vrcode.app.utils.Utils

@SuppressLint("ClickableViewAccessibility", "StaticFieldLeak")
class LorieService : Service() {
    //private
    //static
    var compositor: Long = 0
    private var mTP: TouchParser? = null
    private var session: TermuxSession? = null

    @SuppressLint("BatteryLife,UnspecifiedImmutableFlag")
    override fun onCreate() {
        if (isServiceRunningInForeground(this, LorieService::class.java)) return
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        @SuppressLint("SdCardPath") val Xdgpath =
            preferences.getString("CustXDG", "/data/data/com.termux/files/usr/tmp/")
        compositor = createLorieThread(Xdgpath)
        if (compositor == 0L) {
            Log.e("LorieService", "compositor thread was not created")
            return
        }
        instance = this
        Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show()
        Log.e("LorieService", "created")
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.putExtra("foo_bar_extra_key", "foo_bar_extra_value")
        notificationIntent.action = java.lang.Long.toString(System.currentTimeMillis())
        val exitIntent = Intent(applicationContext, LorieService::class.java)
        exitIntent.action = ACTION_STOP_SERVICE
        val preferencesIntent = Intent(applicationContext, LoriePreferences::class.java)
        preferencesIntent.action = ACTION_START_PREFERENCES_ACTIVITY
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingExitIntent = PendingIntent.getService(
            applicationContext, 0, exitIntent, 0
        )
        val pendingPreferencesIntent =
            PendingIntent.getActivity(
                applicationContext, 0, preferencesIntent, 0
            )

        // For creating the Foreground Service
        val priority: Int = NotificationManager.IMPORTANCE_HIGH
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getNotificationChannel(
            notificationManager
        ) else ""
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_x11_icon)
            .setContentText("Pull down to show options")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(priority)
            .setShowWhen(false)
            .setColor(-0x9f8275)
            .addAction(0, "Preferences", pendingPreferencesIntent)
            .addAction(0, "Exit", pendingExitIntent)
            .build()
        startForeground(1, notification)
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val whitelist = Intent()
            whitelist.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            whitelist.data = Uri.parse("package:$packageName")
            whitelist.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(whitelist)
        }

        // Setup Desktop Environment

//        val scriptString = Utils.getAssetScript(Constant.LINUX_ENV_STARTUP_SCRIPT, application)
//        val bashScript = Utils.BashScript(scriptString, true)
//        val client = object : TermuxTerminalSessionClientBase() {
//            override fun onTextChanged(changedSession: TerminalSession?) {
//                Log.d("Session", changedSession?.isRunning.toString())
//            }
//
//            override fun onSessionFinished(finishedSession: TerminalSession?) {
//                Log.d("Session", "session dead")
//            }
//        }
//        val environment = object : TermuxShellEnvironmentClient() {
//            override fun buildEnvironment(
//                currentPackageContext: Context?,
//                isFailSafe: Boolean,
//                workingDirectory: String?
//            ): Array<String> {
//                val list = super.buildEnvironment(
//                    currentPackageContext,
//                    isFailSafe,
//                    workingDirectory
//                ).toMutableList()
//                Constant.XWAYLAND_ENVS.forEach {
//                    list.add(it)
//                }
//                return list.toTypedArray()
//            }
//        }
//        // Log.d("Service", "Start Desktop 2")
//        session = TermuxSession.execute(
//            this,
//            bashScript.get(),
//            client,
//            null,
//            environment,
//            "desktop",
//            true
//        )
//        session!!.terminalSession.initializeEmulator(1, 1)
        // TODO: Here's another fucking trick, it set size to 1,1 and start execute command.
        // With this, there's no need to call JNI function manually.
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(notificationManager: NotificationManager): String {
        val channelId = resources.getString(R.string.app_name)
        val channelName = resources.getString(R.string.app_name)
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.importance = NotificationManager.IMPORTANCE_NONE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e("LorieService", "start")
        val action = intent.action
        if (action == ACTION_START_FROM_ACTIVITY) {
            act!!.onLorieServiceStart(this)
        }
        if (action == ACTION_STOP_SERVICE) {
            Log.e("LorieService", action)
            terminate()
            sleep(500)
            act!!.finish()
            stopSelf()
            System.exit(0) // This is needed to completely finish the process
        }
        onPreferencesChanged()
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        Log.e("LorieService", "destroyed")
    }

    fun setListeners(view: SurfaceView) {
        val a = view.rootView.findViewById<View>(android.R.id.content).context
        if (a !is MainActivity) {
            Log.e("LorieService", "Context is not an activity!!!")
        }
        act = a as MainActivity
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
        listener.svc = this
        listener.setAsListenerTo(view)
        mTP = TouchParser(view, listener)
        onPreferencesChanged()
    }

    fun terminate() {
        terminate(compositor)
        compositor = 0
        Log.e("LorieService", "terminate")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private class ServiceEventListener : SurfaceHolder.Callback, OnTouchListener,
        View.OnKeyListener, OnHoverListener, OnGenericMotionListener, OnTouchParseListener {
        var svc: LorieService? = null

        @SuppressLint("WrongConstant")
        fun setAsListenerTo(view: SurfaceView) {
            view.holder.addCallback(this)
            view.setOnTouchListener(this)
            view.setOnHoverListener(this)
            view.setOnGenericMotionListener(this)
            view.setOnKeyListener(this)
            surfaceChanged(view.holder, PixelFormat.UNKNOWN, view.width, view.height)
        }

        override fun onPointerButton(button: Int, state: Int) {
            if (svc == null) return
            svc!!.pointerButton(button, state)
        }

        override fun onPointerMotion(x: Int, y: Int) {
            if (svc == null) return
            svc!!.pointerMotion(x.toFloat(), y.toFloat())
        }

        override fun onPointerScroll(axis: Int, value: Float) {
            if (svc == null) return
            svc!!.pointerScroll(axis, value)
        }

        override fun onTouchDown(id: Int, x: Float, y: Float) {
            if (svc == null) return
            svc!!.touchDown(id, x, y)
        }

        override fun onTouchMotion(id: Int, x: Float, y: Float) {
            if (svc == null) return
            svc!!.touchMotion(id, x, y)
        }

        override fun onTouchUp(id: Int) {
            if (svc == null) return
            svc!!.touchUp(id)
        }

        override fun onTouchFrame() {
            if (svc == null) return
            svc!!.touchFrame()
        }

        override fun onTouch(v: View, e: MotionEvent): Boolean {
            return if (svc == null) false else svc!!.mTP!!.onTouchEvent(e)
        }

        override fun onGenericMotion(v: View, e: MotionEvent): Boolean {
            return if (svc == null) false else svc!!.mTP!!.onTouchEvent(e)
        }

        override fun onHover(v: View, e: MotionEvent): Boolean {
            return if (svc == null) false else svc!!.mTP!!.onTouchEvent(e)
        }

        private fun isSource(e: KeyEvent, source: Int): Boolean {
            return e.source and source == source
        }

        private var rightPressed = false // Prevent right button press event from being repeated
        private var middlePressed = false // Prevent middle button press event from being repeated
        override fun onKey(v: View, keyCode: Int, e: KeyEvent): Boolean {
            if (svc == null) return false
            var action = 0
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (isSource(e, InputDevice.SOURCE_MOUSE) &&
                    rightPressed != (e.action == KeyEvent.ACTION_DOWN)
                ) {
                    svc!!.pointerButton(
                        TouchParser.BTN_RIGHT,
                        if (e.action == KeyEvent.ACTION_DOWN) TouchParser.ACTION_DOWN else TouchParser.ACTION_UP
                    )
                    rightPressed = e.action == KeyEvent.ACTION_DOWN
                } else if (e.action == KeyEvent.ACTION_UP) {
                    if (act!!.kbd != null) act!!.kbd!!.requestFocus()
                    KeyboardUtils.toggleKeyboardVisibility(act)
                }
                return true
            }
            if (keyCode == KeyEvent.KEYCODE_MENU &&
                isSource(
                    e,
                    InputDevice.SOURCE_MOUSE
                ) && middlePressed != (e.action == KeyEvent.ACTION_DOWN)
            ) {
                svc!!.pointerButton(
                    TouchParser.BTN_MIDDLE,
                    if (e.action == KeyEvent.ACTION_DOWN) TouchParser.ACTION_DOWN else TouchParser.ACTION_UP
                )
                middlePressed = e.action == KeyEvent.ACTION_DOWN
                return true
            }
            if (e.action == KeyEvent.ACTION_DOWN) action = TouchParser.ACTION_DOWN
            if (e.action == KeyEvent.ACTION_UP) action = TouchParser.ACTION_UP
            if (e.characters == null) return false
            svc!!.keyboardKey(action, keyCode, if (e.isShiftPressed) 1 else 0, e.characters)
            return true
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            val dm = DisplayMetrics()
            val mmWidth: Int
            val mmHeight: Int
            act!!.windowManager.defaultDisplay.getMetrics(dm)
            if (act!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mmWidth = Math.round(width * 25.4 / dm.xdpi).toInt()
                mmHeight = Math.round(height * 25.4 / dm.ydpi).toInt()
            } else {
                mmWidth = Math.round(width * 25.4 / dm.ydpi).toInt()
                mmHeight = Math.round(height * 25.4 / dm.xdpi).toInt()
            }
            svc!!.windowChanged(holder.surface, width, height, mmWidth, mmHeight)
        }

        override fun surfaceCreated(holder: SurfaceHolder) {}
        override fun surfaceDestroyed(holder: SurfaceHolder) {}
    }

    private fun windowChanged(s: Surface, w: Int, h: Int, pw: Int, ph: Int) {
        windowChanged(compositor, s, w, h, pw, ph)
    }

    private external fun windowChanged(
        compositor: Long,
        surface: Surface,
        width: Int,
        height: Int,
        mmWidth: Int,
        mmHeight: Int
    )

    private fun touchDown(id: Int, x: Float, y: Float) {
        touchDown(compositor, id, x.toInt(), y.toInt())
    }

    private external fun touchDown(compositor: Long, id: Int, x: Int, y: Int)
    private fun touchMotion(id: Int, x: Float, y: Float) {
        touchMotion(compositor, id, x.toInt(), y.toInt())
    }

    private external fun touchMotion(compositor: Long, id: Int, x: Int, y: Int)
    private fun touchUp(id: Int) {
        touchUp(compositor, id)
    }

    private external fun touchUp(compositor: Long, id: Int)
    private external fun touchFrame(compositor: Long = this.compositor)
    private fun pointerMotion(x: Float, y: Float) {
        pointerMotion(compositor, x.toInt(), y.toInt())
    }

    private external fun pointerMotion(compositor: Long, x: Int, y: Int)
    private fun pointerScroll(axis: Int, value: Float) {
        pointerScroll(compositor, axis, value)
    }

    private external fun pointerScroll(compositor: Long, axis: Int, value: Float)
    private fun pointerButton(button: Int, type: Int) {
        pointerButton(compositor, button, type)
    }

    private external fun pointerButton(compositor: Long, button: Int, type: Int)
    private fun keyboardKey(key: Int, type: Int, shift: Int, characters: String) {
        keyboardKey(compositor, key, type, shift, characters)
    }

    private external fun keyboardKey(
        compositor: Long,
        key: Int,
        type: Int,
        shift: Int,
        characters: String
    )

    private external fun createLorieThread(CustXdgpath: String?): Long
    private external fun terminate(compositor: Long)

    companion object {
        const val ACTION_STOP_SERVICE = "com.termux.x11.service_stop"
        const val ACTION_START_FROM_ACTIVITY = "com.termux.x11.start_from_activity"
        const val ACTION_START_PREFERENCES_ACTIVITY = "com.termux.x11.start_preferences_activity"
        const val ACTION_PREFERENCES_CHAGED = "com.termux.x11.preferences_changed"
        private var instance: LorieService? = null
        private val listener = ServiceEventListener()
        private var act: MainActivity? = null
        fun setMainActivity(activity: MainActivity?) {
            act = activity
        }

        @JvmStatic
        fun start(action: String?) {
            val intent = Intent(act, LorieService::class.java)
            intent.action = action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                act!!.startForegroundService(intent)
            } else {
                act!!.startService(intent)
            }
        }

        fun isServiceRunningInForeground(context: Context, serviceClass: Class<*>): Boolean {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return service.foreground
                }
            }
            return false
        }

        private fun onPreferencesChanged() {
            if (instance == null || act == null) return
            val preferences = PreferenceManager.getDefaultSharedPreferences(instance)
            val mode = preferences.getString("touchMode", "1")!!.toInt()
            instance!!.mTP!!.setMode(mode)
            Log.e("LorieService", "Preferences changed")
        }

        fun getInstance(): LorieService? {
            if (instance == null) {
                Log.e("LorieService", "Instance was requested, but no instances available")
            }
            return instance
        }

        val onKeyListener: View.OnKeyListener
            get() = listener

        fun sleep(millis: Long) {
            try {
                Thread.sleep(millis)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        init {
            System.loadLibrary("lorie")
        }
    }

    init {
        instance = this
    }
}