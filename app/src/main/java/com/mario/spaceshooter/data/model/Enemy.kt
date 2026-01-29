package com.mario.spaceshooter.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.mario.spaceshooter.R
import java.util.Random

class Enemy(context: Context, screenX: Int, screenY: Int) {

    // Use your own enemy image (make sure you have ic_enemy or similar in drawable)
    // If you don't have one, use ic_launcher_foreground temporarily
    var bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground)

    var x: Int = 0
    var y: Int = 0
    var speed: Int = 10

    // Hitbox
    private val detectCollision: Rect

    // Limits
    private val maxX: Int
    private val maxY: Int

    init {
        // Reduce enemy size (e.g. 120x120)
        bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)

        maxX = screenX
        maxY = screenY - bitmap.height

        // Initial Spawn:
        // X = -width (hidden to the left of the screen)
        // Y = Random vertical position
        x = -bitmap.width
        val generator = Random()
        y = generator.nextInt(maxY)

        detectCollision = Rect(x, y, x + bitmap.width, y + bitmap.height)
    }

    fun update(playerSpeed: Int) {
        // Move to the right (increase X)
        // PDF: "ships appear... from the left of the screen to the right"
        x += speed

        // Update Hitbox
        detectCollision.left = x
        detectCollision.top = y
        detectCollision.right = x + bitmap.width
        detectCollision.bottom = y + bitmap.height
    }

    fun getCollisionShape(): Rect {
        return detectCollision
    }

    fun getWidth(): Int {
        return bitmap.width
    }
}