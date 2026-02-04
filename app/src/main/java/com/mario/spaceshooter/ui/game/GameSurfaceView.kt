package com.mario.spaceshooter.ui.game

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mario.spaceshooter.data.model.Difficulty
import com.mario.spaceshooter.data.model.Enemy
import com.mario.spaceshooter.data.model.Player
import java.util.concurrent.CopyOnWriteArrayList

class GameSurfaceView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), Runnable {

    // Variables de control del hilo
    private var playing = false
    private var gameThread: Thread? = null
    private val surfaceHolder: SurfaceHolder = holder
    private val paint = Paint()

    // Configuración del juego
    private var playerName: String = ""
    private var difficulty: Difficulty = Difficulty.EASY

    // Variables de pantalla
    private var screenWidth = 0
    private var screenHeight = 0

    // Entidades del juego (¡Esto te faltaba!)
    private var player: Player? = null
    // Usamos CopyOnWriteArrayList para evitar errores si modificamos la lista mientras se dibuja
    private val enemies = CopyOnWriteArrayList<Enemy>()

    // Lógica de spawn (generación de enemigos)
    private var lastEnemySpawnTime: Long = 0

    // Callback para avisar a la Activity de que hemos perdido
    var onGameOverListener: (() -> Unit)? = null

    init {
        // Importante para detectar pulsaciones de teclado
        isFocusable = true
    }

    /**
     * Configurar parámetros iniciales recibidos desde el menú
     */
    fun configureGame(name: String, diff: Difficulty) {
        this.playerName = name
        this.difficulty = diff
    }

    override fun run() {
        while (playing) {
            update()  // Calcular física y movimientos
            draw()    // Dibujar en pantalla
            control() // Controlar los FPS (aprox 60)
        }
    }

    /**
     * Lógica principal del juego
     */
    private fun update() {
        // 1. Actualizar Jugador
        player?.update()

        // 2. Generar Enemigos (Basado en la dificultad)
        if (System.currentTimeMillis() - lastEnemySpawnTime > difficulty.spawnInterval) {
            // Añadimos un enemigo nuevo
            val enemy = Enemy(context, screenWidth, screenHeight)
            enemies.add(enemy)
            lastEnemySpawnTime = System.currentTimeMillis()
        }

        // 3. Actualizar Enemigos y Colisiones
        for (enemy in enemies) {
            // Ajustar velocidad según dificultad [cite: 101]
            enemy.update((enemy.speed * difficulty.enemySpeedMultiplier).toInt())

            // Si sale de la pantalla por la derecha, eliminarlo para ahorrar memoria
            // NOTA: Según tu lógica en Enemy.kt, el enemigo va sumando X.
            if (enemy.x > screenWidth) {
                enemies.remove(enemy)
            }

            // DETECCIÓN DE COLISIÓN (Game Over) [cite: 102]
            if (player != null && Rect.intersects(player!!.getCollisionShape(), enemy.getCollisionShape())) {
                playing = false // Detener el bucle

                // Avisamos a la Activity para que muestre la pantalla de Game Over
                // (Es importante hacerlo en un hilo de UI si vas a mostrar vistas,
                // pero aquí solo invocamos el callback)
                onGameOverListener?.invoke()
            }
        }
    }

    /**
     * Dibujar elementos con Canvas
     */
    private fun draw() {
        if (surfaceHolder.surface.isValid) {
            // Bloquear el canvas para dibujar
            val canvas = surfaceHolder.lockCanvas()

            // 1. Limpiar pantalla (Fondo oscuro)
            canvas.drawColor(Color.parseColor("#050B14"))

            // 2. Dibujar Jugador
            player?.let {
                canvas.drawBitmap(it.bitmap, it.x.toFloat(), it.y.toFloat(), paint)
            }

            // 3. Dibujar Enemigos
            for (enemy in enemies) {
                canvas.drawBitmap(enemy.bitmap, enemy.x.toFloat(), enemy.y.toFloat(), paint)
            }

            // 4. Debug info / HUD
            paint.color = Color.WHITE
            paint.textSize = 50f
            canvas.drawText("Piloto: $playerName", 50f, 100f, paint)
            // canvas.drawText("Enemigos: ${enemies.size}", 50f, 180f, paint)

            // Desbloquear y mostrar lo dibujado
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun control() {
        try {
            Thread.sleep(17) // ~60 FPS
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // --- CONTROLES (Requisito del PDF: Teclado y Ratón) ---

    // 1. Control Táctil / Ratón (Arrastrar para mover)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // ACTION_MOVE sirve tanto para arrastrar el dedo como el ratón pulsado
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
            // Actualizamos la posición Y de la nave para que siga al puntero
            player?.updatePosition(event.y.toInt())
        }
        return true
    }

    // 2. Control por Teclado (Flechas o WASD)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        player?.let {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> it.isGoingUp = true
                KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> it.isGoingDown = true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        player?.let {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> it.isGoingUp = false
                KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> it.isGoingDown = false
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    // --- GESTIÓN DEL HILO Y PANTALLA ---

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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h

        // Reinicializar jugador si cambia la pantalla
        if (player == null) {
            player = Player(context, screenWidth, screenHeight)
        }
    }
}