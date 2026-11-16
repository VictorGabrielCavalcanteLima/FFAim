package dev.venice.ffaim

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

@SuppressLint("SetTextI18n")
class Panel(private val ctx: Context) {

    private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var root: LinearLayout? = null
    private var status: TextView? = null

    fun show() {
        if (root != null) return

        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xDD101010.toInt())
            setPadding(24, 16, 24, 16)
        }

        val header = TextView(ctx).apply {
            text = "FF AIM  ⠿"
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(0, 0, 0, 8)
        }
        box.addView(header)

        status = TextView(ctx).apply {
            setTextColor(0xFF66DD66.toInt())
            textSize = 11f
            text = "red 0  |  0 fps"
        }
        box.addView(status)

        val master = Button(ctx).apply {
            text = "LIGAR"
            setOnClickListener {
                Cfg.enabled = !Cfg.enabled
                text = if (Cfg.enabled) "DESLIGAR" else "LIGAR"
                setTextColor(if (Cfg.enabled) Color.RED else Color.WHITE)
            }
        }
        box.addView(master)

        box.addView(slider("passo", 20, 140, Cfg.step) { Cfg.step = it })
        box.addView(slider("nudge", 0, 30, Cfg.nudge) { Cfg.nudge = it })
        box.addView(slider("cooldown", 200, 1200, Cfg.cooldown.toInt()) { Cfg.cooldown = it.toLong() })
        box.addView(slider("vermelho", 25, 90, Cfg.redDelta) { Cfg.redDelta = it })

        val cal = Button(ctx).apply {
            text = "CALIBRAR"
            setOnClickListener {
                ctx.startActivity(
                    android.content.Intent(ctx, CalibrationActivity::class.java)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
        box.addView(cal)

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 240
        }

        header.setOnTouchListener(object : View.OnTouchListener {
            var dx = 0f
            var dy = 0f
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dx = lp.x - e.rawX
                        dy = lp.y - e.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        lp.x = (dx + e.rawX).toInt()
                        lp.y = (dy + e.rawY).toInt()
                        wm.updateViewLayout(root, lp)
                    }
                }
                return true
            }
        })

        root = box
        wm.addView(box, lp)
    }

    fun tick() {
        status?.text = "red ${Cfg.red}  |  ${Cfg.fps} fps"
    }

    fun hide() {
        root?.let { runCatching { wm.removeView(it) } }
        root = null
    }

    private fun slider(label: String, min: Int, max: Int, cur: Int, onChange: (Int) -> Unit): LinearLayout {
        val row = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        val title = TextView(ctx).apply {
            text = "$label: $cur"
            setTextColor(Color.LTGRAY)
            textSize = 10f
        }
        row.addView(title)
        val bar = SeekBar(ctx).apply {
            this.min = min
            this.max = max
            progress = cur
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                    title.text = "$label: $p"
                    onChange(p)
                }
                override fun onStartTrackingTouch(sb: SeekBar) {}
                override fun onStopTrackingTouch(sb: SeekBar) {}
            })
        }
        row.addView(bar)
        return row
    }
}