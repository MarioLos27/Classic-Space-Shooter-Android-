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

    // Screen variables (initialized when size changes)
    private var screenWidth = 0
    private var screenHeight = 0

    init {
        // Optional: Load heavy resources here if necessary
    }

    /**
     * Configure initial params received from the menu
     */
    fun configureGame(name: String, diff: Difficulty) {
        this.playerName = name
        this.difficulty = diff
        // Here we adjust velocity of enemy spawning depending on the difficulty selected
    }

    override fun run() {
        while (playing) {
            update() // Calculate the movements and physics
            draw()   // Draw on the screen
            control() // Control the FPS (Frames per second)
        }
    }

    /**
     * Game logic
     */
    private fun update() {
        // TODO: Move the player's ship
        // TODO: Generate random enemies according to difficulty
        // TODO: Move enemies and detect collisions
    }

    /**
     * Draw elements with canvas
     */
    private fun draw() {
        if (surfaceHolder.surface.isValid) {
            // Lock the canvas to draw
            val canvas = surfaceHolder.lockCanvas()

            // 1. Clear screen (Paint background)
            // We use a dark space color
            canvas.drawColor(Color.parseColor("#050B14"))

            // 2. Draw test elements (Debugging)
            paint.color = Color.WHITE
            paint.textSize = 50f
            canvas.drawText("Pilot: $playerName", 50f, 100f, paint)
            canvas.drawText("Threat: ${difficulty.name}", 50f, 180f, paint)

            // TODO: Draw ship bitmaps here

            // Unlock the canvas to display what was drawn
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    /**
     * Controls the loop speed (approx 60 FPS)
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

    // Called when the screen size changes (e.g. rotation or startup)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h
        // Here we will initialize the player's position
    }
}