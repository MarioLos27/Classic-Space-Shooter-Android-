package com.mario.spaceshooter.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.mario.spaceshooter.R

class Player(context: Context, screenX: Int, screenY: Int) {

    // CAMBIO: Usamos game_icon (JPG) en lugar de ic_launcher_foreground (XML)
    var bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.game_icon)

    // Coordinates
    var x: Int = 0
    var y: Int = 0

    // ... resto del código igual ...

    private val speed = 20
    var isGoingUp = false
    var isGoingDown = false
    private val maxY: Int
    private val minY: Int
    private val detectCollision: Rect

    init {
        // Resize: Aseguramos que no sea null antes de escalar
        bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false)

        x = screenX - bitmap.width - 50
        y = screenY / 2 - bitmap.height / 2
        maxY = screenY - bitmap.height
        minY = 0
        detectCollision = Rect(x, y, x + bitmap.width, y + bitmap.height)
    }

    // ... resto de métodos update(), updatePosition(), getCollisionShape() ...
    fun update() {
        if (isGoingUp) y -= speed
        if (isGoingDown) y += speed
        if (y < minY) y = minY
        if (y > maxY) y = maxY

        detectCollision.left = x
        detectCollision.top = y
        detectCollision.right = x + bitmap.width
        detectCollision.bottom = y + bitmap.height
    }

    fun updatePosition(newY: Int) {
        y = newY - bitmap.height / 2
        if (y < minY) y = minY
        if (y > maxY) y = maxY
        detectCollision.top = y
        detectCollision.bottom = y + bitmap.height
    }

    fun getCollisionShape(): Rect {
        return detectCollision
    }
}