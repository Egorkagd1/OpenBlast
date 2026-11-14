package com.openblast.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.openblast.game.logic.DragState
import com.openblast.game.model.ThemeConfig

@Composable
fun DraggingPieceOverlay(
    dragState: DragState,
    theme: ThemeConfig,
    cellSizePx: Float,
    verticalOffsetPx: Float,
    modifier: Modifier = Modifier
) {
    if (!dragState.isDragging || dragState.piece == null) return

    val piece = dragState.piece
    val effectiveCellSize = if (cellSizePx > 0f) cellSizePx else 40.dp.value * 2.5f

    Canvas(modifier = modifier.fillMaxSize()) {
        val pieceWidthPx = piece.width * effectiveCellSize
        val pieceHeightPx = piece.height * effectiveCellSize

        // Center piece horizontally on touch, and position it above the finger by verticalOffsetPx
        val startX = dragState.dragPosition.x - pieceWidthPx / 2f
        val startY = dragState.dragPosition.y - verticalOffsetPx - pieceHeightPx / 2f

        BlockDrawing.drawPiece(
            drawScope = this,
            piece = piece,
            topLeft = Offset(startX, startY),
            cellSize = effectiveCellSize,
            theme = theme,
            alpha = 0.95f
        )
    }
}
