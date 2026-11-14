package com.openblast.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.openblast.game.model.GameState
import com.openblast.game.model.Piece
import com.openblast.game.model.ThemeConfig

@Composable
fun PieceDockView(
    gameState: GameState,
    draggingPieceIndex: Int,
    onDragStart: (Int, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dockPieces = gameState.dockPieces
    val theme = gameState.theme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 3) {
            val piece = dockPieces.getOrNull(i)
            val isBeingDragged = draggingPieceIndex == i

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (piece != null) {
                    DockPieceItem(
                        piece = piece,
                        theme = theme,
                        pieceIndex = i,
                        isBeingDragged = isBeingDragged,
                        canFitOnBoard = gameState.board.canPlaceAnywhere(piece),
                        onDragStart = onDragStart,
                        onDragMove = onDragMove,
                        onDragEnd = onDragEnd
                    )
                }
            }
        }
    }
}

@Composable
private fun DockPieceItem(
    piece: Piece,
    theme: ThemeConfig,
    pieceIndex: Int,
    isBeingDragged: Boolean,
    canFitOnBoard: Boolean,
    onDragStart: (Int, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var itemCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var currentTouchPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size(100.dp)
            .onGloballyPositioned { coordinates ->
                itemCoordinates = coordinates
            }
            .pointerInput(piece.id) {
                detectDragGestures(
                    onDragStart = { localOffset ->
                        val coords = itemCoordinates
                        val initialPos = if (coords != null && coords.isAttached) {
                            coords.positionInRoot() + localOffset
                        } else {
                            localOffset
                        }
                        currentTouchPosition = initialPos
                        onDragStart(pieceIndex, currentTouchPosition)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentTouchPosition += dragAmount
                        onDragMove(currentTouchPosition)
                    },
                    onDragEnd = {
                        onDragEnd()
                    },
                    onDragCancel = {
                        onDragEnd()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (!isBeingDragged) {
            Canvas(modifier = Modifier.size(90.dp)) {
                val maxDim = maxOf(piece.width, piece.height).coerceAtLeast(1)
                val blockSize = (size.width / maxDim).coerceAtMost(28.dp.toPx())

                val pieceW = piece.width * blockSize
                val pieceH = piece.height * blockSize

                val startX = (size.width - pieceW) / 2f
                val startY = (size.height - pieceH) / 2f

                val alpha = if (canFitOnBoard) 1f else 0.45f

                BlockDrawing.drawPiece(
                    drawScope = this,
                    piece = piece,
                    topLeft = Offset(startX, startY),
                    cellSize = blockSize,
                    theme = theme,
                    alpha = alpha
                )
            }
        }
    }
}
