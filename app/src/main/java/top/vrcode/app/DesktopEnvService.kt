package top.vrcode.app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.termux.shared.shell.TermuxSession
import com.termux.shared.shell.TermuxShellEnvironmentClient
import com.termux.shared.terminal.TermuxTerminalSessionClientBase
import com.termux.terminal.TerminalSession
import top.vrcode.app.utils.Utils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Exception
import kotlin.concurrent.thread

class DesktopEnvService : Service() {
    var inited = false
    var session: TermuxSession? = null

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constant.DESKTOP_SESSION_NOTIFICATION_KEY,
                getString(R.string.desktop_notification_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val notification =
            NotificationCompat.Builder(this, Constant.DESKTOP_SESSION_NOTIFICATION_KEY)
                .setSmallIcon(R.drawable.ic_x11_icon)
                .setContentTitle(getString(R.string.desktop_notification_name))
                .setContentText(getString(R.string.desktop_notification_content))
                .setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setShowWhen(false)
                .setColor(0xFF607D8B.toInt())
                .build()

        startForeground(Constant.DESKTOP_SESSION_NOTIFICATION_ID, notification)
    }

    @SuppressLint("SdCardPath")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (inited) return START_REDELIVER_INTENT else inited = true
        if (intent == null) return START_REDELIVER_INTENT
        if (session != null) return START_REDELIVER_INTENT


        Log.d("Service", "Start Desktop")

        val desktop = intent.getStringExtra(Constant.DESKTOP_TYPE_INTENT_KEY)
        if (desktop != null && desktop in Constant.AVAILABLE_DESKTOP_ENVS) {
//            val runtime = Runtime.getRuntime()
//            val envs = arrayOf(
//                "DISPLAY=:1",
//                "XDG_RUNTIME_DIR=/data/data/com.termux/files/usr/tmp"
//            )
//            val args = arrayOf(
//                "Xwayland",
//                ":1"
//            )
//            val args2 = arrayOf(
//                "xfce4-session"
//            )
//            val location = File("/data/data/com.termux/files/usr/bin")
//            for (file in location.list()!!) {
//                Log.d("files", file)
//                Utils.setPermission("/data/data/com.termux/files/usr/bin/$file")
//            }
//            runtime.exec("/data/data/com.termux/files/usr/bin/Xwayland :1", envs, location)
//            val xfce =
//                runtime.exec("/data/data/com.termux/files/usr/bin/xfce4-session", envs, location)
//
//            val ism = xfce.errorStream
//            val isr = InputStreamReader(ism)
//            val br = BufferedReader(isr)
//            thread {
//                var line: String?
//                while (br.readLine().also { line = it } != null) line?.let { Log.d("Wayland", it) }
//            }


            val scriptString =
                application.assets.open(Constant.DESKTOP_ENV_STARTUP_SCRIPTS[desktop]!!)
                    .bufferedReader().use {
                        it.readText()
                    }
            val bashScript = Utils.BashScript(scriptString, true)
            val client = object : TermuxTerminalSessionClientBase() {
                override fun onTextChanged(changedSession: TerminalSession?) {
                    Log.d("Session", changedSession?.isRunning.toString())
                }

                override fun onSessionFinished(finishedSession: TerminalSession?) {
                    Log.d("Session", "session dead")
                }
            }
            val environment = object : TermuxShellEnvironmentClient() {
                override fun buildEnvironment(
                    currentPackageContext: Context?,
                    isFailSafe: Boolean,
                    workingDirectory: String?
                ): Array<String> {
                    val list = super.buildEnvironment(
                        currentPackageContext,
                        isFailSafe,
                        workingDirectory
                    ).toMutableList()
                    list.add("DISPLAY=:1")
                    list.add("XDG_RUNTIME_DIR=/data/data/com.termux/files/usr/tmp")
                    return list.toTypedArray()
                }
            }
            Log.d("Service", "Start Desktop 2")
            session = TermuxSession.execute(
                this,
                bashScript.get(),
                client,
                null,
                environment,
                "desktop",
                true
            )
            session!!.terminalSession.updateSize(1, 1)
            // TODO: Here's another fucking trick, it set size to 1,1 and start execute command.
            // With this, there's no need to call JNI function manually.

            //            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
//                Log.d("Service", "terminal running is ${session.terminalSession.isRunning}")
//            }, 300, 300, TimeUnit.MILLISECONDS)

        } else {
            throw Exception("Unknown Desktop Env type $desktop")
        }
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}