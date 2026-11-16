package dev.venice.ffaim

import android.graphics.Bitmap
import android.hardware.input.IInputManager
import android.hardware.input.InputManager
import android.os.Build
import android.os.SystemClock
import android.view.InputDevice
import android.view.InputEvent
import android.view.MotionEvent
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Injector {

    private val im: IInputManager by lazy {
        IInputManager.Stub.asInterface(
            ShizukuBinderWrapper(SystemServiceHelper.getSystemService("input"))
        )
    }

    private fun send(ev: InputEvent) {
        ev.setDisplayId(0)
        im.injectInputEvent(ev, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC)
    }

    private fun event(down: Long, t: Long, action: Int, x: Float, y: Float): MotionEvent {
        val e = MotionEvent.obtain(down, t, action, x, y, 0)
        e.source = InputDevice.SOURCE_TOUCHSCREEN
        return e
    }

    fun drag(x0: Float, y0: Float, x1: Float, y1: Float, ms: Long) {
        val down = SystemClock.uptimeMillis()
        event(down, down, MotionEvent.ACTION_DOWN, x0, y0).let { send(it); it.recycle() }
        val steps = 4
        for (i in 1..steps) {
            val x = x0 + (x1 - x0) * i / steps
            val y = y0 + (y1 - y0) * i / steps
            event(down, down + ms * i / steps, MotionEvent.ACTION_MOVE, x, y)
                .let { send(it); it.recycle() }
            Thread.sleep(ms / steps)
        }
        event(down, down + ms, MotionEvent.ACTION_UP, x1, y1).let { send(it); it.recycle() }
    }

    fun tap(x: Float, y: Float, ms: Long) {
        val down = SystemClock.uptimeMillis()
        event(down, down, MotionEvent.ACTION_DOWN, x, y).let { send(it); it.recycle() }
        Thread.sleep(ms)
        event(down, down + ms, MotionEvent.ACTION_UP, x, y).let { send(it); it.recycle() }
    }

    /** Captura de alta performance (SurfaceControl) com fallback pra screencap tradicional */
    fun screencap(): Triple<Int, Int, ByteArray>? {
        return screencapSurfaceControl() ?: screencapProcess()
    }

    private fun screencapSurfaceControl(): Triple<Int, Int, ByteArray>? {
        return try {
            val scClass = Class.forName("android.view.SurfaceControl")
            val bitmap = if (Build.VERSION.SDK_INT >= 28) {
                val method = scClass.getMethod("screenshot", android.graphics.Rect::class.java, Int::class.java, Int::class.java, Int::class.java)
                method.invoke(null, null, 0, 0, 0) as? Bitmap
            } else {
                null
            } ?: return null

            val w = bitmap.width
            val h = bitmap.height
            val buffer = ByteBuffer.allocate(w * h * 4)
            bitmap.copyPixelsToBuffer(buffer)
            val raw = buffer.array()
            Triple(w, h, raw)
        } catch (t: Throwable) {
            null
        }
    }

    private fun screencapProcess(): Triple<Int, Int, ByteArray>? {
        return try {
            val p = Shizuku.newProcess(arrayOf("/system/bin/screencap"), null, null)
            val raw = p.inputStream.readBytes()
            p.waitFor()
            if (raw.size < 16) return null
            val bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN)
            val w = bb.int
            val h = bb.int
            bb.int
            if (w <= 0 || h <= 0 || raw.size < 12 + w * h * 4) null
            else Triple(w, h, raw)
        } catch (t: Throwable) {
            null
        }
    }
}
