package top.vrcode.app

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
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DesktopEnvService : Service() {
    var inited = false
    lateinit var session: TermuxSession

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (inited) return START_STICKY else inited = true

        Log.d("Service", "Start Desktop")

        val desktop = intent?.getStringExtra(Constant.DESKTOP_TYPE_INTENT_KEY)
        if (desktop != null && desktop in Constant.AVAILABLE_DESKTOP_ENVS) {
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