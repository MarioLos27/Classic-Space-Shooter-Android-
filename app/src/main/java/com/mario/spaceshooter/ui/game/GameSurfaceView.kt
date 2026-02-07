package com.mario.spaceshooter.ui.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.mario.spaceshooter.data.model.Bullet
import com.mario.spaceshooter.data.model.Difficulty
import com.mario.spaceshooter.data.model.Enemy
import com.mario.spaceshooter.data.model.Player
import java.util.concurrent.CopyOnWriteArrayList

class GameSurfaceView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), Runnable {

    // --- VARIABLES DE CONTROL ---
    private var playing = false
    private var gameThread: Thread? = null
    private val surfaceHolder: SurfaceHolder = holder
    private val paint = Paint()

    // --- CONFIGURACIÓN ---
    private var playerName: String = ""
    private var difficulty: Difficulty = Difficulty.EASY

    // --- PANTALLA ---
    private var screenWidth = 0
    private var screenHeight = 0

    // --- ENTIDADES ---
    private var player: Player? = null
    private val enemies = CopyOnWriteArrayList<Enemy>()
    private val bullets = CopyOnWriteArrayList<Bullet>()

    // --- TIEMPOS ---
    private var lastEnemySpawnTime: Long = 0
    private var lastShotTime: Long = 0
    private var gameStartTime: Long = 0

    // --- SONIDOS ---
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()
    private var collisionSoundId: Int = 0
    // private var shootSoundId: Int = 0 // Descomentar si añades sonido de disparo

    // --- CALLBACKS ---
    var onGameOverListener: (() -> Unit)? = null

    // --- FONDO ---
    private var bgBitmap: Bitmap? = null

    init {
        isFocusable = true
        // Cargar sonidos (asegúrate de que existen en res/raw)
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

        // 1. ACTUALIZAR JUGADOR
        player?.update()

        // 2. DISPARAR AUTOMÁTICAMENTE (Hacia la izquierda)
        // Cadencia: 400ms
        if (System.currentTimeMillis() - lastShotTime > 400) {
            player?.let {
                // Altura: Centro de la nave
                val bulletY = it.y + (it.bitmap.height / 2)

                // Posición X: El morro de la nave está a la IZQUIERDA (porque la nave está a la derecha de la pantalla)
                // Restamos un poco (20px) para que salga justo del frente
                val bulletX = it.x - 20

                bullets.add(Bullet(bulletX, bulletY))
                lastShotTime = System.currentTimeMillis()

                // Reproducir sonido disparo (opcional)
                // if (shootSoundId != 0) soundPool.play(shootSoundId, 0.5f, 0.5f, 0, 0, 1f)
            }
        }

        // 3. ACTUALIZAR BALAS
        for (bullet in bullets) {
            bullet.update() // La bala se mueve hacia la izquierda (x -= speed)

            // Eliminar bala si sale por la IZQUIERDA de la pantalla (< 0)
            if (bullet.x < 0) {
                bullets.remove(bullet)
            }
        }

        // 4. GENERAR ENEMIGOS (Dificultad progresiva)
        val timeElapsed = System.currentTimeMillis() - gameStartTime
        val timeMultiplier = 1.0f + (timeElapsed / 10000f) * 0.1f
        val currentSpawnInterval = (difficulty.spawnInterval / timeMultiplier).toLong()

        if (System.currentTimeMillis() - lastEnemySpawnTime > currentSpawnInterval) {
            val enemy = Enemy(context, screenWidth, screenHeight)
            enemies.add(enemy)
            lastEnemySpawnTime = System.currentTimeMillis()
        }

        // 5. ACTUALIZAR ENEMIGOS Y COLISIONES
        for (enemy in enemies) {
            val newSpeed = (10 * difficulty.enemySpeedMultiplier * timeMultiplier).toInt()
            enemy.speed = newSpeed
            enemy.update(newSpeed) // Los enemigos van hacia la derecha

            // A) Eliminar si sale por la DERECHA (screenWidth)
            // Nota: Como tus enemigos van hacia la derecha, comprobamos si superan el ancho
            if (enemy.x > screenWidth) {
                enemies.remove(enemy)
                continue
            }

            // B) COLISIÓN JUGADOR vs ENEMIGO (Game Over)
            if (player != null && Rect.intersects(player!!.getCollisionShape(), enemy.getCollisionShape())) {
                handleGameOver()
            }

            // C) COLISIÓN BALA vs ENEMIGO
            for (bullet in bullets) {
                if (Rect.intersects(bullet.getCollisionShape(), enemy.getCollisionShape())) {
                    // 1. Eliminar enemigo y bala
                    enemies.remove(enemy)
                    bullets.remove(bullet)

                    // 2. Reproducir sonido explosión
                    if (collisionSoundId != 0) {
                        // Pitch 1.5 para que suene más agudo que el choque de nave
                        soundPool.play(collisionSoundId, 0.6f, 0.6f, 0, 0, 1.5f)
                    }

                    // Break para dejar de comprobar esta bala (ya no existe)
                    break
                }
            }
        }
    }

    private fun handleGameOver() {
        if (!playing) return
        playing = false

        if (collisionSoundId != 0) {
            soundPool.play(collisionSoundId, 1f, 1f, 0, 0, 1f)
        }
        onGameOverListener?.invoke()
    }

    private fun draw() {
        if (surfaceHolder.surface.isValid) {
            val canvas = surfaceHolder.lockCanvas()

            // 1. DIBUJAR FONDO
            if (bgBitmap != null) {
                canvas.drawBitmap(bgBitmap!!, 0f, 0f, null)
            } else {
                canvas.drawColor(Color.BLACK)
            }

            // 2. DIBUJAR JUGADOR
            player?.let {
                canvas.drawBitmap(it.bitmap, it.x.toFloat(), it.y.toFloat(), paint)
            }

            // 3. DIBUJAR ENEMIGOS
            for (enemy in enemies) {
                canvas.drawBitmap(enemy.bitmap, enemy.x.toFloat(), enemy.y.toFloat(), paint)
            }

            // 4. DIBUJAR BALAS (Láser Amarillo)
            paint.color = Color.YELLOW
            for (bullet in bullets) {
                canvas.drawRect(bullet.getCollisionShape(), paint)
            }

            // 5. HUD (Texto)
            paint.color = Color.WHITE
            paint.textSize = 40f

            // Nombre del piloto
            canvas.drawText("Piloto: $playerName", 50f, 80f, paint)

            // Tiempo de supervivencia
            val seconds = (System.currentTimeMillis() - gameStartTime) / 1000
            canvas.drawText("Tiempo: ${seconds}s", 50f, 140f, paint)

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

    // --- CONTROLES TÁCTILES ---
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
            player?.updatePosition(event.y.toInt())
        }
        return true
    }

    // --- CONTROLES DE TECLADO (OPCIONAL) ---
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

    // --- GESTIÓN DEL HILO ---
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
        if (gameStartTime == 0L) gameStartTime = System.currentTimeMillis()

        gameThread = Thread(this)
        gameThread?.start()
    }

    // --- INICIALIZACIÓN DE TAMAÑO ---
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h

        // Cargar y escalar el fondo
        try {
            val original = BitmapFactory.decodeResource(resources, R.drawable.game_background)
            if (original != null) {
                bgBitmap = Bitmap.createScaledBitmap(original, w, h, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Crear jugador si no existe
        if (player == null) {
            player = Player(context, screenWidth, screenHeight)
        }
    }
}