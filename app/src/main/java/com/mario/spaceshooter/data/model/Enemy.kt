package com.mario.spaceshooter.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import com.mario.spaceshooter.R
import java.util.Random

class Enemy(context: Context, screenX: Int, screenY: Int) {

    var bitmap: Bitmap
    var x: Int = 0
    var y: Int = 0
    var speed: Int = 10
    private val detectCollision: Rect
    private val maxX: Int
    private val maxY: Int

    init {
        // 1. POOL DE IMÁGENES
        // Asegúrate de tener estas imágenes en res/drawable
        val enemySkins = listOf(
            R.drawable.enemy_1,
            R.drawable.enemy_2,
            R.drawable.enemy_3,
            R.drawable.enemy_4,
            R.drawable.enemy_5
        )

        // 2. SELECCIONAR SKIN ALEATORIA
        val randomSkinId = enemySkins.random()
        var originalBitmap = BitmapFactory.decodeResource(context.resources, randomSkinId)

        // Fallback de seguridad por si la imagen no existe
        if (originalBitmap == null) {
            originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.game_icon)
        }

        // 3. ROTAR LA IMAGEN 90 GRADOS A LA DERECHA
        val matrix = Matrix()
        // 90f gira la imagen en sentido horario (de Arriba -> Derecha)
        matrix.postRotate(90f)

        val rotatedBitmap = Bitmap.createBitmap(
            originalBitmap,
            0, 0,
            originalBitmap.width, originalBitmap.height,
            matrix,
            true
        )

        // 4. ESCALAR
        // Ajustamos el tamaño final después de rotar
        bitmap = Bitmap.createScaledBitmap(rotatedBitmap, 120, 120, false)

        maxX = screenX
        maxY = screenY - bitmap.height

        // Posición inicial: Fuera de la pantalla por la izquierda
        x = -bitmap.width

        // Posición Y aleatoria segura
        val safeMaxY = if (maxY > 0) maxY else 1
        val generator = Random()
        y = generator.nextInt(safeMaxY)

        detectCollision = Rect(x, y, x + bitmap.width, y + bitmap.height)
    }

    fun update(playerSpeed: Int) {
        // Mover hacia la derecha
        x += speed

        // Actualizar hitbox
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