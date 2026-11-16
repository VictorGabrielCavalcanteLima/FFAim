package dev.venice.ffaim

import android.os.SystemClock

class AimEngine {

    @Volatile private var running = false
    private var thread: Thread? = null

    fun start() {
        if (running) return
        running = true
        thread = Thread { loop() }.also { it.start() }
    }

    fun stop() {
        running = false
        thread?.join(1500)
        thread = null
    }

    private fun loop() {
        var dir = 1
        var hits = 0
        var lastFire = 0L
        var frames = 0
        var mark = SystemClock.elapsedRealtime()

        while (running) {
            if (!Cfg.enabled) {
                Cfg.fps = 0
                Cfg.red = 0
                Thread.sleep(200)
                continue
            }

            val cap = Injector.screencap()
            if (cap == null) {
                Thread.sleep(30)
                continue
            }

            val (w, h, raw) = cap
            val red = Vision.countRed(raw, w, h, Cfg.redDelta)
            Cfg.red = red

            frames++
            val now = SystemClock.elapsedRealtime()
            if (now - mark >= 1000) {
                Cfg.fps = frames
                frames = 0
                mark = now
            }

            hits = if (red > 3) hits + 1 else 0

            val cx = w / 2f
            val cy = h / 2f
            val fx = w * Cfg.fireX
            val fy = h * Cfg.fireY

            if (hits >= 2 && SystemClock.elapsedRealtime() - lastFire > Cfg.cooldown) {
                Injector.drag(cx, cy, cx, cy - Cfg.nudge, 30)
                Thread.sleep(20 + (Math.random() * 30).toLong())
                Injector.tap(fx, fy, 35)
                lastFire = SystemClock.elapsedRealtime()
                hits = 0
                Thread.sleep(120)
            } else {
                Injector.drag(cx, cy, cx + dir * Cfg.step, cy, Cfg.stepMs)
                if (Math.random() < 0.12) dir = -dir
            }
        }
    }
}