package com.mario.spaceshooter.data.model

import java.io.Serializable

enum class Difficulty(val spawnInterval: Long, val enemySpeedMultiplier: Float) : Serializable {
    EASY(2000L, 1.0f),      // Enemy's speed: 2s
    MEDIUM(1500L, 1.5f),    // Enemy's speed: 1.5s, 50% faster
    HARD(800L, 2.0f)        // Enemy's speed: 0.8s, double faster
}