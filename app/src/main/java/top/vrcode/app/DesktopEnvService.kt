package top.vrcode.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.termux.shared.shell.TermuxSession
import com.termux.shared.shell.TermuxShellEnvironmentClient
import com.termux.shared.terminal.TermuxTerminalSessionClientBase
import top.vrcode.app.utils.Utils
import java.lang.Exception

class DesktopEnvService : Service() {
    var inited = false

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
                .setContentTitle(getString(R.string.desktop_notification_name))
                .build()
        startForeground(Constant.DESKTOP_SESSION_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (inited) return START_STICKY else inited = true

        val desktop = intent?.getStringExtra(Constant.DESKTOP_TYPE_INTENT_KEY)
        if (desktop != null && desktop in Constant.AVAILABLE_DESKTOP_ENVS) {
            val scriptString =
                application.assets.open(Constant.DESKTOP_ENV_STARTUP_SCRIPTS[desktop]!!)
                    .bufferedReader().use {
                        it.readText()
                    }
            val bashScript = Utils.BashScript(scriptString)
            TermuxSession.execute(
                this,
                bashScript.get(),
                TermuxTerminalSessionClientBase(),
                null,
                TermuxShellEnvironmentClient(),
                "desktop",
                true
            )
        } else {
            throw Exception("Unknown Desktop Env type $desktop")
        }
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}