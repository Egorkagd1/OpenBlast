package com.openblast.game.logic

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openblast.game.model.Board
import com.openblast.game.model.GameState
import com.openblast.game.model.OpenBlastThemes
import com.openblast.game.model.Particle
import com.openblast.game.model.Piece
import com.openblast.game.model.ScorePopup
import com.openblast.game.model.ThemeConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class DragState(
    val isDragging: Boolean = false,
    val pieceIndex: Int = -1,
    val piece: Piece? = null,
    val dragPosition: Offset = Offset.Zero,
    val hoverRow: Int? = null,
    val hoverCol: Int? = null,
    val canPlace: Boolean = false
)

class GameEngine(private val context: Context) : ViewModel() {

    private val prefs = context.getSharedPreferences("openblast_prefs", Context.MODE_PRIVATE)
    val soundManager = SoundManager(context)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _dragState = MutableStateFlow(DragState())
    val dragState: StateFlow<DragState> = _dragState.asStateFlow()

    init {
        val savedBestScore = prefs.getInt("best_score", 0)
        val savedThemeId = prefs.getString("theme_id", OpenBlastThemes.CLASSIC.id) ?: OpenBlastThemes.CLASSIC.id
        val theme = OpenBlastThemes.getById(savedThemeId, context)

        _gameState.update {
            it.copy(
                bestScore = savedBestScore,
                theme = theme
            )
        }
        startNewGame()
    }

    fun startNewGame() {
        val newBoard = Board()
        val initialPieces = PieceGenerator.generatePieces(
            board = newBoard,
            count = 3,
            maxColors = _gameState.value.theme.pieceColors.size
        )

        _gameState.update {
            it.copy(
                board = newBoard,
                dockPieces = initialPieces,
                score = 0,
                comboCount = 0,
                streakCount = 0,
                isGameOver = false,
                blastingCells = emptySet(),
                scorePopups = emptyList(),
                particles = emptyList()
            )
        }
    }

    fun setTheme(theme: ThemeConfig) {
        prefs.edit().putString("theme_id", theme.id).apply()
        _gameState.update { it.copy(theme = theme) }
    }

    // Drag & Drop Handling
    fun onDragStart(pieceIndex: Int, startPosition: Offset) {
        val piece = _gameState.value.dockPieces.getOrNull(pieceIndex) ?: return
        soundManager.playPickUp()
        _dragState.value = DragState(
            isDragging = true,
            pieceIndex = pieceIndex,
            piece = piece,
            dragPosition = startPosition
        )
    }

    fun onDragMove(position: Offset, boardRow: Int?, boardCol: Int?) {
        val currentPiece = _dragState.value.piece ?: return
        val currentBoard = _gameState.value.board

        val canPlace = if (boardRow != null && boardCol != null) {
            currentBoard.canPlace(currentPiece, boardRow, boardCol)
        } else false

        _dragState.update {
            it.copy(
                dragPosition = position,
                hoverRow = if (canPlace) boardRow else null,
                hoverCol = if (canPlace) boardCol else null,
                canPlace = canPlace
            )
        }
    }

    fun onDragEnd(boardWidthPx: Float = 0f, cellSizePx: Float = 0f) {
        val currentDrag = _dragState.value
        val piece = currentDrag.piece
        val pieceIndex = currentDrag.pieceIndex
        val targetRow = currentDrag.hoverRow
        val targetCol = currentDrag.hoverCol

        _dragState.value = DragState() // Reset drag

        if (piece != null && targetRow != null && targetCol != null && currentDrag.canPlace) {
            placePiece(pieceIndex, piece, targetRow, targetCol, cellSizePx)
        }
    }

    private fun placePiece(
        pieceIndex: Int,
        piece: Piece,
        row: Int,
        col: Int,
        cellSizePx: Float
    ) {
        val state = _gameState.value
        val updatedBoard = state.board.place(piece, row, col)

        // Base points: 10 per block
        val piecePoints = piece.blockCount * 10
        var newScore = state.score + piecePoints

        // Check line completions
        val completed = updatedBoard.findCompletedLines()

        val newDockPieces = state.dockPieces.toMutableList()
        newDockPieces[pieceIndex] = null

        if (completed.isEmpty) {
            // No lines cleared
            soundManager.playPlace()
            val newBest = maxOf(state.bestScore, newScore)
            saveBestScore(newBest)

            _gameState.update {
                it.copy(
                    board = updatedBoard,
                    dockPieces = newDockPieces,
                    score = newScore,
                    bestScore = newBest,
                    comboCount = 0
                )
            }
            checkRoundOrGameOver(updatedBoard, newDockPieces)
        } else {
            // Lines cleared! Combo!
            val newCombo = state.comboCount + 1
            soundManager.playBlast(newCombo)

            // Calculate combo score
            val lineBonus = 100 * completed.totalLines * newCombo
            newScore += lineBonus
            val newBest = maxOf(state.bestScore, newScore)
            saveBestScore(newBest)

            // Gather all cells that get blasted
            val blastedCells = mutableSetOf<Pair<Int, Int>>()
            for (r in completed.rows) {
                for (c in 0 until 8) blastedCells.add(r to c)
            }
            for (c in completed.cols) {
                for (r in 0 until 8) blastedCells.add(r to c)
            }

            // Spawn particles & score popups
            val newParticles = createParticlesForBlast(blastedCells, cellSizePx, state.theme)
            val popupText = if (newCombo > 1) {
                "+$lineBonus (COMBO x$newCombo!)"
            } else {
                "+$lineBonus"
            }
            val newPopup = ScorePopup(
                text = popupText,
                row = completed.rows.firstOrNull() ?: row,
                col = completed.cols.firstOrNull() ?: col,
                color = state.theme.accentColor
            )

            _gameState.update {
                it.copy(
                    board = updatedBoard,
                    dockPieces = newDockPieces,
                    score = newScore,
                    bestScore = newBest,
                    comboCount = newCombo,
                    blastingCells = blastedCells,
                    scorePopups = it.scorePopups + newPopup,
                    particles = it.particles + newParticles
                )
            }

            // Animate blast then remove cells from grid
            viewModelScope.launch {
                delay(220) // Flash duration
                val boardAfterClear = updatedBoard.clearLines(completed.rows, completed.cols)
                _gameState.update {
                    it.copy(
                        board = boardAfterClear,
                        blastingCells = emptySet()
                    )
                }
                checkRoundOrGameOver(boardAfterClear, newDockPieces)

                // Fade out popups
                delay(600)
                _gameState.update {
                    it.copy(
                        scorePopups = it.scorePopups.filter { p -> p.id != newPopup.id },
                        particles = emptyList()
                    )
                }
            }
        }
    }

    private fun checkRoundOrGameOver(currentBoard: Board, dockPieces: List<Piece?>) {
        // If all 3 pieces used, spawn new trio
        var availablePieces = dockPieces.filterNotNull()
        var updatedPieces = dockPieces

        if (availablePieces.isEmpty()) {
            val freshPieces = PieceGenerator.generatePieces(
                board = currentBoard,
                count = 3,
                maxColors = _gameState.value.theme.pieceColors.size
            )
            updatedPieces = freshPieces
            availablePieces = freshPieces
            _gameState.update { it.copy(dockPieces = freshPieces) }
        }

        // Check Game Over
        val canMakeMove = currentBoard.hasValidMove(availablePieces)
        if (!canMakeMove) {
            soundManager.playGameOver()
            _gameState.update { it.copy(isGameOver = true) }
        }
    }

    private fun createParticlesForBlast(
        cells: Set<Pair<Int, Int>>,
        cellSizePx: Float,
        theme: ThemeConfig
    ): List<Particle> {
        val particles = mutableListOf<Particle>()
        val effectiveCellSize = if (cellSizePx > 0f) cellSizePx else 40f

        for ((r, c) in cells) {
            val cx = c * effectiveCellSize + effectiveCellSize / 2f
            val cy = r * effectiveCellSize + effectiveCellSize / 2f

            // 6-8 particles per cleared cell
            val count = Random.nextInt(4, 7)
            for (i in 0 until count) {
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val speed = Random.nextFloat() * 8f + 2f
                val color = theme.particleColors.random()
                particles.add(
                    Particle(
                        x = cx,
                        y = cy,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        color = color,
                        radius = Random.nextFloat() * 4f + 2f
                    )
                )
            }
        }
        return particles
    }

    private fun saveBestScore(bestScore: Int) {
        prefs.edit().putInt("best_score", bestScore).apply()
    }
}
