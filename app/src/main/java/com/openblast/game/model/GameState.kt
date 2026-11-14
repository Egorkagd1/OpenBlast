package com.openblast.game.model

data class ScorePopup(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    val text: String,
    val row: Int,
    val col: Int,
    val color: androidx.compose.ui.graphics.Color
)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: androidx.compose.ui.graphics.Color,
    val radius: Float,
    val alpha: Float = 1f,
    val life: Float = 1f
)

data class GameState(
    val board: Board = Board(),
    val dockPieces: List<Piece?> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val comboCount: Int = 0,
    val streakCount: Int = 0,
    val isGameOver: Boolean = false,
    val theme: ThemeConfig = OpenBlastThemes.CLASSIC,
    val blastingCells: Set<Pair<Int, Int>> = emptySet(),
    val scorePopups: List<ScorePopup> = emptyList(),
    val particles: List<Particle> = emptyList()
)
