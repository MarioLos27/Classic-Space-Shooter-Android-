package com.mario.spaceshooter.ui.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mario.spaceshooter.R
import com.mario.spaceshooter.data.model.Difficulty
import com.mario.spaceshooter.ui.game.GameActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etPlayerName: EditText
    private lateinit var rgDifficulty: RadioGroup
    private lateinit var btnStartGame: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // View binding
        etPlayerName = findViewById(R.id.etPlayerName)
        rgDifficulty = findViewById(R.id.rgDifficulty)
        btnStartGame = findViewById(R.id.btnStartGame)

        // Configuration for start button
        btnStartGame.setOnClickListener {
            startGame()
        }
    }

    private fun startGame() {
        val name = etPlayerName.text.toString().trim()

        // Validates that name is mandatory
        if (name.isEmpty()) {
            etPlayerName.error = "¡Piloto, identifícate!"
            etPlayerName.requestFocus()
            return
        }

        // Here we get the difficulty selected
        val difficulty = when (rgDifficulty.checkedRadioButtonId) {
            R.id.rbEasy -> Difficulty.EASY
            R.id.rbMedium -> Difficulty.MEDIUM
            R.id.rbHard -> Difficulty.HARD
            else -> Difficulty.EASY // Por defecto
        }

        // Intent to the game
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("PLAYER_NAME", name)
            putExtra("DIFFICULTY", difficulty) // Gracias a Serializable podemos pasar el objeto completo
        }

        startActivity(intent)
    }
}