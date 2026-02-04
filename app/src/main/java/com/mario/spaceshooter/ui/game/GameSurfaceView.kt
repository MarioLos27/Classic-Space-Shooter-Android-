package com.mario.spaceshooter.ui.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.SoundPool
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mario.spaceshooter.R
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

    // Entidades del juego
    private var player: Player? = null
    private val enemies = CopyOnWriteArrayList<Enemy>()

    // Gestión de tiempo y dificultad
    private var lastEnemySpawnTime: Long = 0
    private var gameStartTime: Long = 0

    // Sonidos
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()
    private var collisionSoundId: Int = 0

    // Callback para Game Over
    var onGameOverListener: (() -> Unit)? = null

    init {
        isFocusable = true
        // Cargar el sonido de colisión (asegúrate de que collision.mp3 está en res/raw)
        collisionSoundId = soundPool.load(context, R.raw.collision, 1)
    }

    fun configureGame(name: String, diff: Difficulty) {
        this.playerName = name
        this.difficulty = diff
    }

    override fun run() {
        while (playing) {
            update()
            draw()
            control()
        }
    }

    private fun update() {
        if (screenHeight <= 0 || screenWidth <= 0) return

        // 1. Actualizar Jugador
        player?.update()

        // ... resto del código igual ...

        // 2. Calcular factor de tiempo (Aumenta dificultad progresivamente)
        // Cada 10 segundos, la velocidad aumenta un 10% (aprox)
        val timeElapsed = System.currentTimeMillis() - gameStartTime
        val timeMultiplier = 1.0f + (timeElapsed / 10000f) * 0.1f

        // 3. Generar Enemigos
        // Reducimos el intervalo de aparición con el tiempo (más frenético)
        val currentSpawnInterval = (difficulty.spawnInterval / timeMultiplier).toLong()

        if (System.currentTimeMillis() - lastEnemySpawnTime > currentSpawnInterval) {
            val enemy = Enemy(context, screenWidth, screenHeight)
            enemies.add(enemy)
            lastEnemySpawnTime = System.currentTimeMillis()
        }

        // 4. Actualizar Enemigos y Colisiones
        for (enemy in enemies) {
            // Calcular nueva velocidad basada en dificultad base y tiempo transcurrido
            // enemy.speed base es 10. Multiplicamos por dificultad y por tiempo.
            val newSpeed = (10 * difficulty.enemySpeedMultiplier * timeMultiplier).toInt()
            enemy.speed = newSpeed

            // Llamamos a update (el parámetro se ignora en tu clase Enemy actual,
            // pero ya hemos actualizado la propiedad .speed arriba)
            enemy.update(newSpeed)

            // Eliminar si sale de pantalla
            if (enemy.x > screenWidth) {
                enemies.remove(enemy)
            }

            // DETECCIÓN DE COLISIÓN
            if (player != null && Rect.intersects(player!!.getCollisionShape(), enemy.getCollisionShape())) {
                handleGameOver()
            }
        }
    }

    private fun handleGameOver() {
        if (!playing) return // Evitar llamadas múltiples
        playing = false

        // Reproducir sonido de explosión
        if (collisionSoundId != 0) {
            soundPool.play(collisionSoundId, 1f, 1f, 0, 0, 1f)
        }

        // Avisar a la Activity
        onGameOverListener?.invoke()
    }

    private fun draw() {
        if (surfaceHolder.surface.isValid) {
            val canvas = surfaceHolder.lockCanvas()
            canvas.drawColor(Color.parseColor("#050B14"))

            player?.let {
                canvas.drawBitmap(it.bitmap, it.x.toFloat(), it.y.toFloat(), paint)
            }

            for (enemy in enemies) {
                canvas.drawBitmap(enemy.bitmap, enemy.x.toFloat(), enemy.y.toFloat(), paint)
            }

            // HUD: Mostrar nombre y tiempo
            paint.color = Color.WHITE
            paint.textSize = 40f
            canvas.drawText("Piloto: $playerName", 50f, 80f, paint)

            // Mostrar tiempo de supervivencia
            val seconds = (System.currentTimeMillis() - gameStartTime) / 1000
            canvas.drawText("Tiempo: ${seconds}s", 50f, 140f, paint)

            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun control() {
        try {
            Thread.sleep(17)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // --- CONTROLES ---
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
            player?.updatePosition(event.y.toInt())
        }
        return true
    }

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

    // --- GESTIÓN ---
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
        // Reiniciar el contador de tiempo si es una partida nueva o continuar
        if (gameStartTime == 0L) gameStartTime = System.currentTimeMillis()

        gameThread = Thread(this)
        gameThread?.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h
        if (player == null) {
            player = Player(context, screenWidth, screenHeight)
        }
    }
}