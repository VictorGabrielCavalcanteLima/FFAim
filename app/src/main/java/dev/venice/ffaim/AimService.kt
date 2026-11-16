package dev.venice.ffaim

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class AimService : Service() {

    private var engine: AimEngine? = null
    private var panel: Panel? = null
    private val ui = Handler(Looper.getMainLooper())

    private val ticker = object : Runnable {
        override fun run() {
            panel?.tick()
            ui.postDelayed(this, 500)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        val ch = NotificationChannel("aim", "FF Aim", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)

        val n: Notification = Notification.Builder(this, "aim")
            .setContentTitle("FF Aim ativo")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
        startForeground(1, n)

        engine = AimEngine().also { it.start() }
        panel = Panel(this).also { it.show() }
        ui.post(ticker)
    }

    override fun onDestroy() {
        Cfg.enabled = false
        ui.removeCallbacks(ticker)
        engine?.stop()
        panel?.hide()
        super.onDestroy()
    }
}