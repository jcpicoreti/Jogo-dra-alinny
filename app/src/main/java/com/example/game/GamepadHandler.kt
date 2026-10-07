package com.example.game

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

class GamepadHandler {
    var moveX: Float = 0f
        private set
    var moveY: Float = 0f
        private set

    // D-Pad key states
    private var dpadUp = false
    private var dpadDown = false
    private var dpadLeft = false
    private var dpadRight = false

    var onButtonAPressed: (() -> Unit)? = null
    var onButtonBPressed: (() -> Unit)? = null
    var onButtonStartPressed: (() -> Unit)? = null

    fun handleMotionEvent(event: MotionEvent): Boolean {
        if ((event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
            event.action == MotionEvent.ACTION_MOVE
        ) {
            val axisX = getCenteredAxis(event, MotionEvent.AXIS_X)
            val axisY = getCenteredAxis(event, MotionEvent.AXIS_Y)
            val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)

            val finalX = if (Math.abs(axisX) > 0.15f) axisX else hatX
            val finalY = if (Math.abs(axisY) > 0.15f) axisY else hatY

            if (Math.abs(finalX) > 0.1f || Math.abs(finalY) > 0.1f) {
                moveX = finalX
                moveY = finalY
                return true
            } else if (!isDpadActive()) {
                moveX = 0f
                moveY = 0f
            }
        }
        return false
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        val isDown = event.action == KeyEvent.ACTION_DOWN

        when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                dpadUp = isDown
                updateDpadMovement()
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                dpadDown = isDown
                updateDpadMovement()
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                dpadLeft = isDown
                updateDpadMovement()
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                dpadRight = isDown
                updateDpadMovement()
                return true
            }
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (isDown) onButtonAPressed?.invoke()
                return true
            }
            KeyEvent.KEYCODE_BUTTON_B -> {
                if (isDown) onButtonBPressed?.invoke()
                return true
            }
            KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_MENU -> {
                if (isDown) onButtonStartPressed?.invoke()
                return true
            }
        }
        return false
    }

    private fun isDpadActive(): Boolean = dpadUp || dpadDown || dpadLeft || dpadRight

    private fun updateDpadMovement() {
        var x = 0f
        var y = 0f
        if (dpadRight) x += 1f
        if (dpadLeft) x -= 1f
        if (dpadDown) y += 1f
        if (dpadUp) y -= 1f

        val len = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        if (len > 0f) {
            moveX = x / len
            moveY = y / len
        } else {
            moveX = 0f
            moveY = 0f
        }
    }

    private fun getCenteredAxis(event: MotionEvent, axis: Int): Float {
        val range = event.device?.getMotionRange(axis, event.source) ?: return 0f
        val value = event.getAxisValue(axis)
        val flat = range.flat
        return if (Math.abs(value) > flat) value else 0f
    }
}
