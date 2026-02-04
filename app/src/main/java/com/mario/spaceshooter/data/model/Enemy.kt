package com.mario.spaceshooter.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.mario.spaceshooter.R
import java.util.Random
import kotlin.math.max

class Enemy(context: Context, screenX: Int, screenY: Int) {

    // Usamos el icono provisional para evitar errores de XML
    var bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.game_icon)

    var x: Int = 0
    var y: Int = 0
    var speed: Int = 10
    private val detectCollision: Rect
    private val maxX: Int
    private val maxY: Int

    init {
        // Reducir tamaño
        bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)

        maxX = screenX
        // Calculamos el límite vertical
        maxY = screenY - bitmap.height

        // Posición inicial X (fuera de pantalla a la izquierda)
        x = -bitmap.width

        // CORRECCIÓN DEL ERROR:
        // Si maxY es negativo o 0 (pantalla no cargada), usamos 1 para evitar el crash
        val safeMaxY = if (maxY > 0) maxY else 1

        val generator = Random()
        y = generator.nextInt(safeMaxY)

        detectCollision = Rect(x, y, x + bitmap.width, y + bitmap.height)
    }

    fun update(playerSpeed: Int) {
        x += speed
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