package com.mario.spaceshooter.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.mario.spaceshooter.R

class Player(context: Context, screenX: Int, screenY: Int) {

    // Imagen de la nave
    var bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground) // Usa tu icono o imagen propia

    // Coordenadas
    var x: Int = 0
    var y: Int = 0

    // Velocidad de movimiento
    private val speed = 20 // Píxeles por frame

    // Variables de control
    var isGoingUp = false
    var isGoingDown = false

    // Límites de la pantalla para no salirse
    private val maxY: Int
    private val minY: Int

    // Hitbox para colisiones
    private val detectCollision: Rect

    init {
        // Redimensionar la nave si es muy grande (opcional, ej: 100x100 px)
        bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false)

        // Posición inicial: Izquierda centrado verticalmente (según tu PDF la tuya está a la DERECHA)
        // PDF: "A la derecha de la pantalla se visualizará mi nave"
        x = screenX - bitmap.width - 50 // Margen derecho
        y = screenY / 2 - bitmap.height / 2

        maxY = screenY - bitmap.height
        minY = 0

        detectCollision = Rect(x, y, x + bitmap.width, y + bitmap.height)
    }

    fun update() {
        // Movimiento exclusivo vertical (Arriba/Abajo)
        if (isGoingUp) {
            y -= speed
        }
        if (isGoingDown) {
            y += speed
        }

        // Control de límites (No salir de pantalla)
        if (y < minY) y = minY
        if (y > maxY) y = maxY

        // Actualizar Hitbox
        detectCollision.left = x
        detectCollision.top = y
        detectCollision.right = x + bitmap.width
        detectCollision.bottom = y + bitmap.height
    }

    // Método para cambiar posición absoluta (para el Ratón/Touch)
    fun updatePosition(newY: Int) {
        y = newY - bitmap.height / 2

        // Control de límites
        if (y < minY) y = minY
        if (y > maxY) y = maxY

        detectCollision.top = y
        detectCollision.bottom = y + bitmap.height
    }

    fun getCollisionShape(): Rect {
        return detectCollision
    }
}