package dev.venice.ffaim

object Vision {

    const val HALF = 13

    fun countRed(raw: ByteArray, w: Int, h: Int, delta: Int): Int {
        val cx = w / 2
        val cy = h / 2
        var n = 0
        for (y in cy - HALF..cy + HALF) {
            for (x in cx - HALF..cx + HALF) {
                val i = (y * w + x) * 4
                if (i + 2 >= raw.size) continue
                val r = raw[i].toInt() and 255
                val g = raw[i + 1].toInt() and 255
                val b = raw[i + 2].toInt() and 255
                if (r > 140 && r - g > delta && r - b > delta) n++
            }
        }
        return n
    }
}