package com.mario.spaceshooter.ui.game

import android.content.Context
import android.graphics.Bitmap // Importante
import android.graphics.BitmapFactory // Importante
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

    // --- FONDO DEL JUEGO ---
    // Declaramos la variable aquí para usarla en toda la clase
    private var bgBitmap: Bitmap? = null

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

        // 2. Calcular factor de tiempo (Aumenta dificultad progresivamente)
        val timeElapsed = System.currentTimeMillis() - gameStartTime
        val timeMultiplier = 1.0f + (timeElapsed / 10000f) * 0.1f

        // 3. Generar Enemigos
        val currentSpawnInterval = (difficulty.spawnInterval / timeMultiplier).toLong()

        if (System.currentTimeMillis() - lastEnemySpawnTime > currentSpawnInterval) {
            val enemy = Enemy(context, screenWidth, screenHeight)
            enemies.add(enemy)
            lastEnemySpawnTime = System.currentTimeMillis()
        }

        // 4. Actualizar Enemigos y Colisiones
        for (enemy in enemies) {
            val newSpeed = (10 * difficulty.enemySpeedMultiplier * timeMultiplier).toInt()
            enemy.speed = newSpeed
            enemy.update(newSpeed)

            if (enemy.x > screenWidth) {
                enemies.remove(enemy)
            }

            if (player != null && Rect.intersects(player!!.getCollisionShape(), enemy.getCollisionShape())) {
                handleGameOver()
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

            // --- DIBUJAR FONDO ---
            if (bgBitmap != null) {
                // Dibujamos la imagen en la posición 0,0
                canvas.drawBitmap(bgBitmap!!, 0f, 0f, null)
            } else {
                // Si falla la carga, fondo negro por seguridad
                canvas.drawColor(Color.BLACK)
            }

            // Dibujar Jugador
            player?.let {
                canvas.drawBitmap(it.bitmap, it.x.toFloat(), it.y.toFloat(), paint)
            }

            // Dibujar Enemigos
            for (enemy in enemies) {
                canvas.drawBitmap(enemy.bitmap, enemy.x.toFloat(), enemy.y.toFloat(), paint)
            }

            // HUD
            paint.color = Color.WHITE
            paint.textSize = 40f
            canvas.drawText("Piloto: $playerName", 50f, 80f, paint)

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

    // --- AQUÍ SE INICIALIZA EL FONDO ---
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h

        // Lógica para cargar y escalar el fondo usando 'w' y 'h'
        try {
            val original = BitmapFactory.decodeResource(resources, R.drawable.game_background)
            if (original != null) {
                bgBitmap = Bitmap.createScaledBitmap(original, w, h, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (player == null) {
            player = Player(context, screenWidth, screenHeight)
        }
    }
}