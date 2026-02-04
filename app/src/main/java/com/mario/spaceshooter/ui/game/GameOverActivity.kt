package com.mario.spaceshooter.ui.game

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mario.spaceshooter.R
import com.mario.spaceshooter.ui.menu.MainActivity

class GameOverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val btnReturn = findViewById<Button>(R.id.btnReturnMenu)
        btnReturn.setOnClickListener {
            // Go back to the main menu
            val intent = Intent(this, MainActivity::class.java)
            // Cleans the stack of activities, so players cannot go back to game over activity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}