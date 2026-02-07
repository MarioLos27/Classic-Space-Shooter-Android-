package com.mario.spaceshooter.data.model

import android.graphics.Rect

class Bullet(startX: Int, startY: Int) {

    // Posición y velocidad
    var x = startX
    var y = startY
    var speed = 30

    // Dimensiones del láser
    val width = 50
    val height = 10

    // Rectángulo para detectar colisiones
    private val collisionShape = Rect(x, y, x + width, y + height)

    fun update() {
        x -= speed

        // Actualizamos la caja de colisión
        collisionShape.left = x
        collisionShape.top = y
        collisionShape.right = x + width
        collisionShape.bottom = y + height
    }

    fun getCollisionShape(): Rect {
        return collisionShape
    }
}