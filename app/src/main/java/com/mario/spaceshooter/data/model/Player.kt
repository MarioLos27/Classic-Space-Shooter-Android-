package com.mario.spaceshooter.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.mario.spaceshooter.R

class Player(context: Context, screenX: Int, screenY: Int) {

    // SpaceShip image
    var bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground) // Use your own icon or image

    // Coordinates
    var x: Int = 0
    var y: Int = 0

    // Movement speed
    private val speed = 20 // Pixels per frame

    // Control variables
    var isGoingUp = false
    var isGoingDown = false

    // Screen limits to prevent going off-screen
    private val maxY: Int
    private val minY: Int

    // Hitbox for collisions
    private val detectCollision: Rect

    init {
        // Resize the ship if it's too large (optional, e.g.: 100x100 px)
        bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false)

        // Initial position: Left centered vertically (according to your PDF yours is on the RIGHT)
        // PDF: "My ship will be displayed on the right side of the screen"
        x = screenX - bitmap.width - 50 // Right margin
        y = screenY / 2 - bitmap.height / 2

        maxY = screenY - bitmap.height
        minY = 0

        detectCollision = Rect(x, y, x + bitmap.width, y + bitmap.height)
    }

    fun update() {
        // Exclusive vertical movement (Up/Down)
        if (isGoingUp) {
            y -= speed
        }
        if (isGoingDown) {
            y += speed
        }

        // Boundary control (Don't go off-screen)
        if (y < minY) y = minY
        if (y > maxY) y = maxY

        // Update Hitbox
        detectCollision.left = x
        detectCollision.top = y
        detectCollision.right = x + bitmap.width
        detectCollision.bottom = y + bitmap.height
    }

    // Method to change absolute position (for Mouse/Touch)
    fun updatePosition(newY: Int) {
        y = newY - bitmap.height / 2

        // Boundary control
        if (y < minY) y = minY
        if (y > maxY) y = maxY

        detectCollision.top = y
        detectCollision.bottom = y + bitmap.height
    }

    fun getCollisionShape(): Rect {
        return detectCollision
    }
}