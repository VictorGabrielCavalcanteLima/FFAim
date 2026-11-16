package dev.venice.ffaim

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private lateinit var info: TextView

    private val permListener =
        Shizuku.OnRequestPermissionResultListener { _, code ->
            info.text = if (code == 0) "Shizuku: liberado" else "Shizuku: negado ($code)"
        }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 120, 48, 48)
        }

        info = TextView(this).apply {
            text = "1. conceda overlay\n2. autorize o Shizuku\n3. inicie o painel"
            textSize = 14f
            setPadding(0, 0, 0, 40)
        }
        box.addView(info)

        box.addView(Button(this).apply {
            text = "Overlay"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
            }
        })

        box.addView(Button(this).apply {
            text = "Shizuku"
            setOnClickListener {
                if (!Shizuku.isPreV11() && Shizuku.pingBinder()) {
                    if (Shizuku.checkSelfPermission() == 0) info.text = "Shizuku: já liberado"
                    else Shizuku.requestPermission(0)
                } else {
                    info.text = "Shizuku não está rodando"
                }
            }
        })

        box.addView(Button(this).apply {
            text = "Iniciar painel"
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= 33) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 9)
                }
                startForegroundService(Intent(this@MainActivity, AimService::class.java))
                finish()
            }
        })

        setContentView(box)
        Shizuku.addRequestPermissionResultListener(permListener)
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permListener)
        super.onDestroy()
    }
}
