package com.mario.spaceshooter.ui.game

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.mario.spaceshooter.R
import com.mario.spaceshooter.data.model.Difficulty

class GameActivity : AppCompatActivity() {

    private lateinit var gameSurfaceView: GameSurfaceView
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Load the XML layout
        setContentView(R.layout.activity_game)

        // 2. Hide system bars for fullscreen mode (Immersive)
        hideSystemUI()

        // 3. Find the game view by its correct ID
        gameSurfaceView = findViewById(R.id.gameSurfaceView)

        // 4. Receive data from Intent (Menu)
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "Pilot"

        // Retrieve the Difficulty object safely according to Android version
        val difficulty = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("DIFFICULTY", Difficulty::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("DIFFICULTY") as? Difficulty
        } ?: Difficulty.EASY

        // CONFIGURAR LISTENER DE GAME OVER
        gameSurfaceView.onGameOverListener = {
            runOnUiThread {
                gameOver()
            }
        }

        // 5. Start the game engine
        gameSurfaceView.configureGame(playerName, difficulty)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        gameSurfaceView.resume()

        // INICIAR MÚSICA DE FONDO
        if (mediaPlayer == null) {
            // Carga el archivo desde res/raw/asteroid_attack.mp3
            mediaPlayer = MediaPlayer.create(this, R.raw.asteroid_attack)
            mediaPlayer?.isLooping = true // Para que se repita la música
            mediaPlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        gameSurfaceView.pause()

        // PAUSAR Y LIBERAR RECURSOS DE MÚSICA
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun gameOver() {
        // Detener música inmediatamente antes de cambiar de pantalla
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer?.release()
        mediaPlayer = null

        // Ir a la pantalla de Game Over
        val intent = Intent(this, GameOverActivity::class.java)
        startActivity(intent)
        finish() // Cierra la actividad del juego para que no se pueda volver atrás
    }

    private fun hideSystemUI() {
        // Code to hide status bar and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Old versions (optional, but recommended if testing on old devices)
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }
}