package dev.venice.ffaim

object Cfg {
    @Volatile var enabled = false
    @Volatile var step = 55
    @Volatile var stepMs = 40L
    @Volatile var nudge = 10
    @Volatile var cooldown = 550L
    @Volatile var redDelta = 55
    @Volatile var fireX = 0.86f
    @Volatile var fireY = 0.80f

    // telemetria pro painel
    @Volatile var red = 0
    @Volatile var fps = 0
}