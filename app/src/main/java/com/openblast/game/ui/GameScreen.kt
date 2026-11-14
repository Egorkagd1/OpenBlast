package com.openblast.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import com.openblast.game.ui.components.BlockDrawing
import com.openblast.game.logic.GameEngine
import com.openblast.game.ui.components.BoardView
import com.openblast.game.ui.components.DraggingPieceOverlay
import com.openblast.game.ui.components.GameOverDialog
import com.openblast.game.ui.components.PieceDockView
import com.openblast.game.ui.components.ScoreHeader
import com.openblast.game.ui.components.ThemeSelectorDialog
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    gameEngine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (BlockDrawing.tilkaioBitmap == null) {
            try {
                val bm = BitmapFactory.decodeResource(context.resources, com.openblast.game.R.drawable.tilkaio_sprite)
                if (bm != null) {
                    BlockDrawing.tilkaioBitmap = bm.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val gameState by gameEngine.gameState.collectAsState()
    val dragState by gameEngine.dragState.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var isSoundEnabled by remember { mutableStateOf(gameEngine.soundManager.isSoundEnabled) }
    var isHapticsEnabled by remember { mutableStateOf(gameEngine.soundManager.isHapticsEnabled) }

    var boardTopLeft by remember { mutableStateOf(Offset.Zero) }
    var boardWidthPx by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val verticalOffsetPx = with(density) { 135.dp.toPx() }
    val cellSizePx = if (boardWidthPx > 0f) boardWidthPx / 8f else with(density) { 42.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gameState.theme.backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: Score, High score, Themes, Info & Settings buttons
            ScoreHeader(
                gameState = gameState,
                onOpenThemes = { showThemeDialog = true },
                onOpenInfo = { showInfoDialog = true },
                onOpenSettings = { showSettingsDialog = true },
                onRestart = { gameEngine.startNewGame() }
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Center: 8x8 Board
            BoardView(
                gameState = gameState,
                dragState = dragState,
                onBoardCoordinatesChanged = { offset, width ->
                    boardTopLeft = offset
                    boardWidthPx = width
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom: Dock with 3 draggable pieces
            PieceDockView(
                gameState = gameState,
                draggingPieceIndex = dragState.pieceIndex,
                onDragStart = { index, touchPos ->
                    gameEngine.onDragStart(index, touchPos)
                },
                onDragMove = { touchPos ->
                    val piece = dragState.piece
                    if (piece != null && boardWidthPx > 0f && cellSizePx > 0f) {
                        val pieceW = piece.width * cellSizePx
                        val pieceH = piece.height * cellSizePx

                        // Snapped target piece top-left based on touch + finger offset
                        val targetTopLeftX = touchPos.x - pieceW / 2f
                        val targetTopLeftY = touchPos.y - verticalOffsetPx - pieceH / 2f

                        val relativeX = targetTopLeftX - boardTopLeft.x
                        val relativeY = targetTopLeftY - boardTopLeft.y

                        val col = (relativeX / cellSizePx).roundToInt()
                        val row = (relativeY / cellSizePx).roundToInt()

                        if (row in 0 until 8 && col in 0 until 8) {
                            gameEngine.onDragMove(touchPos, row, col)
                        } else {
                            gameEngine.onDragMove(touchPos, null, null)
                        }
                    } else {
                        gameEngine.onDragMove(touchPos, null, null)
                    }
                },
                onDragEnd = {
                    gameEngine.onDragEnd(boardWidthPx, cellSizePx)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Floating Drag Overlay (drawn directly across full screen at root coordinates)
        DraggingPieceOverlay(
            dragState = dragState,
            theme = gameState.theme,
            cellSizePx = cellSizePx,
            verticalOffsetPx = verticalOffsetPx
        )

        // Game Over Dialog
        GameOverDialog(
            gameState = gameState,
            onRestart = { gameEngine.startNewGame() }
        )

        // Theme Selector Modal
        if (showThemeDialog) {
            ThemeSelectorDialog(
                currentTheme = gameState.theme,
                onSelectTheme = { selectedTheme ->
                    gameEngine.setTheme(selectedTheme)
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        // Info Modal (Vibe Coding Notice)
        if (showInfoDialog) {
            com.openblast.game.ui.components.InfoDialog(
                theme = gameState.theme,
                onDismiss = { showInfoDialog = false }
            )
        }

        // Settings Modal
        if (showSettingsDialog) {
            com.openblast.game.ui.components.SettingsDialog(
                theme = gameState.theme,
                isSoundEnabled = isSoundEnabled,
                isHapticsEnabled = isHapticsEnabled,
                onToggleSound = { enabled ->
                    isSoundEnabled = enabled
                    gameEngine.soundManager.isSoundEnabled = enabled
                },
                onToggleHaptics = { enabled ->
                    isHapticsEnabled = enabled
                    gameEngine.soundManager.isHapticsEnabled = enabled
                },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}
