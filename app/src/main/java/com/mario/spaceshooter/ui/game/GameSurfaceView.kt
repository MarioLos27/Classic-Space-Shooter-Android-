package com.mario.spaceshooter.ui.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mario.spaceshooter.data.model.Difficulty

class GameSurfaceView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), Runnable {

    private var playing = false
    private var gameThread: Thread? = null
    private val surfaceHolder: SurfaceHolder = holder
    private val paint = Paint()

    // Game's configuration
    private var playerName: String = ""
    private var difficulty: Difficulty = Difficulty.EASY

    // Variables de pantalla (se inicializan al cambiar el tamaño)
    private var screenWidth = 0
    private var screenHeight = 0

    init {
        // Opcional: Cargar aquí recursos pesados si fuera necesario
    }

    /**
     * Configurate initial params received from the menu
     */
    fun configureGame(name: String, diff: Difficulty) {
        this.playerName = name
        this.difficulty = diff
        // Here we adjust velocity of enemy spawning depending on the difficulty selected
    }

    override fun run() {
        while (playing) {
            update() // Calculate the movements and physics
            draw()   // 2. Dibujar en la pantalla
            control() // 3. Controlar los FPS (Frames por segundo)
        }
    }

    /**
     * Game logic
     */
    private fun update() {
        // TODO: Mover la nave del jugador
        // TODO: Generar enemigos aleatorios según dificultad
        // TODO: Mover enemigos y detectar colisiones
    }

    /**
     * draw elements with canvas
     */
    private fun draw() {
        if (surfaceHolder.surface.isValid) {
            // Bloqueamos el canvas para dibujar
            val canvas = surfaceHolder.lockCanvas()

            // 1. Limpiar pantalla (Pintar fondo)
            // Usamos un color oscuro espacial
            canvas.drawColor(Color.parseColor("#050B14"))

            // 2. Dibujar elementos de prueba (Debugging)
            paint.color = Color.WHITE
            paint.textSize = 50f
            canvas.drawText("Piloto: $playerName", 50f, 100f, paint)
            canvas.drawText("Amenaza: ${difficulty.name}", 50f, 180f, paint)

            // TODO: Dibujar bitmaps de naves aquí

            // Desbloqueamos el canvas para mostrar lo dibujado
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    /**
     * Controla la velocidad del bucle (aprox 60 FPS)
     */
    private fun control() {
        try {
            Thread.sleep(17) // 1000ms / 60fps ≈ 17ms
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun pause() {
        playing = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        playing = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    // Se llama cuando cambia el tamaño de la pantalla (ej. rotación o inicio)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h
        // Aquí inicializaremos la posición del jugador
    }
}