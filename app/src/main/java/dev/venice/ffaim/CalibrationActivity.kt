package dev.venice.ffaim

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity

class CalibrationActivity : ComponentActivity() {

    private val ui = Handler(Looper.getMainLooper())
    @Volatile private var alive = true

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 120, 48, 48)
        }

        val big = TextView(this).apply { textSize = 64f; text = "0" }
        val hint = TextView(this).apply {
            textSize = 13f
            text = "mire num inimigo: o número tem que passar de 4.\nse ficar 0 em inimigo, baixe o limiar."
        }

        box.addView(big)
        box.addView(hint)

        val bar = SeekBar(this).apply {
            min = 25
            max = 90
            progress = Cfg.redDelta
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar, p: Int, u: Boolean) { Cfg.redDelta = p }
                override fun onStartTrackingTouch(sb: SeekBar) {}
                override fun onStopTrackingTouch(sb: SeekBar) {}
            })
        }
        box.addView(bar)

        box.addView(Button(this).apply {
            text = "Fechar"
            setOnClickListener { finish() }
        })

        setContentView(box)

        Thread {
            while (alive) {
                val cap = Injector.screencap()
                val v = if (cap == null) -1
                else Vision.countRed(cap.third, cap.first, cap.second, Cfg.redDelta)
                ui.post { big.text = if (v < 0) "?" else v.toString() }
                Thread.sleep(150)
            }
        }.start()
    }

    override fun onDestroy() {
        alive = false
        super.onDestroy()
    }
}
