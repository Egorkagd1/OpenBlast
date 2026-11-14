package com.openblast.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import com.openblast.game.logic.DragState
import com.openblast.game.model.BlockStyleType
import com.openblast.game.model.GameState
import com.openblast.game.model.StripInfo

@Composable
fun BoardView(
    gameState: GameState,
    dragState: DragState,
    modifier: Modifier = Modifier,
    onBoardCoordinatesChanged: (Offset, Float) -> Unit = { _, _ -> }
) {
    val theme = gameState.theme
    val board = gameState.board
    val gridSize = board.size

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(theme.boardBackground)
            .border(2.dp, theme.boardBorderColor, RoundedCornerShape(20.dp))
            .padding(10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    val pos = coordinates.positionInRoot()
                    val width = coordinates.size.width.toFloat()
                    onBoardCoordinatesChanged(pos, width)
                }
        ) {
            val totalWidth = size.width
            val cellSize = totalWidth / gridSize
            val cellRadius = CornerRadius(cellSize * 0.18f, cellSize * 0.18f)

            // 1. Draw empty grid cells
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val cellTopLeft = Offset(c * cellSize, r * cellSize)
                    val pad = cellSize * 0.06f
                    val drawTopLeft = Offset(cellTopLeft.x + pad, cellTopLeft.y + pad)
                    val drawSize = Size(cellSize - pad * 2, cellSize - pad * 2)

                    drawRoundRect(
                        color = theme.cellEmptyColor,
                        topLeft = drawTopLeft,
                        size = drawSize,
                        cornerRadius = cellRadius
                    )

                    drawRoundRect(
                        color = theme.cellEmptyBorderColor,
                        topLeft = drawTopLeft,
                        size = drawSize,
                        cornerRadius = cellRadius,
                        style = Stroke(width = cellSize * 0.03f)
                    )
                }
            }

            // 2. Draw occupied blocks on the board
            if (theme.styleType == BlockStyleType.TILKAJO && BlockDrawing.tilkaioBitmap != null) {
                val bm = BlockDrawing.tilkaioBitmap!!
                val occupiedCells = mutableListOf<Pair<Int, Int>>()
                for (r in 0 until gridSize) {
                    for (c in 0 until gridSize) {
                        if (board.grid[r][c] != null) {
                            occupiedCells.add(r to c)
                        }
                    }
                }

                val groups = occupiedCells.groupBy { pos ->
                    board.cellStrips[pos] ?: StripInfo(pos.first, pos.second, 1, 1)
                }

                for ((strip, cells) in groups) {
                    val stripTL = Offset(strip.c * cellSize, strip.r * cellSize)
                    val stripW = strip.w * cellSize
                    val stripH = strip.h * cellSize

                    val nonBlastingCells = cells.filter { !gameState.blastingCells.contains(it) }
                    val blastingCells = cells.filter { gameState.blastingCells.contains(it) }

                    // Blasting cells
                    for ((r, c) in blastingCells) {
                        val pad = cellSize * 0.04f
                        val cellTL = Offset(c * cellSize + pad, r * cellSize + pad)
                        val cellSz = Size(cellSize - pad * 2, cellSize - pad * 2)
                        drawRoundRect(
                            color = Color.White,
                            topLeft = cellTL,
                            size = cellSz,
                            cornerRadius = CornerRadius(cellSize * 0.22f, cellSize * 0.22f)
                        )
                    }

                    if (nonBlastingCells.isNotEmpty()) {
                        val stripPath = Path()
                        for ((r, c) in nonBlastingCells) {
                            val pad = cellSize * 0.04f
                            val cellTL = Offset(c * cellSize + pad, r * cellSize + pad)
                            val cellSz = Size(cellSize - pad * 2, cellSize - pad * 2)
                            stripPath.addRoundRect(
                                RoundRect(
                                    rect = Rect(cellTL, cellSz),
                                    cornerRadius = CornerRadius(cellSize * 0.22f, cellSize * 0.22f)
                                )
                            )
                        }

                        clipPath(stripPath) {
                            drawRect(
                                color = Color(0xFFF4A261).copy(alpha = 0.85f),
                                topLeft = stripTL,
                                size = Size(stripW, stripH)
                            )
                            drawImage(
                                image = bm,
                                dstOffset = IntOffset(stripTL.x.roundToInt(), stripTL.y.roundToInt()),
                                dstSize = IntSize(stripW.roundToInt(), stripH.roundToInt()),
                                alpha = 1f
                            )
                        }

                        for ((r, c) in nonBlastingCells) {
                            val pad = cellSize * 0.04f
                            val cellTL = Offset(c * cellSize + pad, r * cellSize + pad)
                            val cellSz = Size(cellSize - pad * 2, cellSize - pad * 2)
                            drawRoundRect(
                                color = Color(0xFF5E3023).copy(alpha = 0.45f),
                                topLeft = cellTL,
                                size = cellSz,
                                cornerRadius = CornerRadius(cellSize * 0.22f, cellSize * 0.22f),
                                style = Stroke(width = cellSize * 0.04f)
                            )
                        }
                    }
                }
            } else {
                for (r in 0 until gridSize) {
                    for (c in 0 until gridSize) {
                        val colorIdx = board.grid[r][c]
                        if (colorIdx != null) {
                            val isBlasting = gameState.blastingCells.contains(r to c)
                            val cellTopLeft = Offset(c * cellSize, r * cellSize)
                            val pad = cellSize * 0.04f
                            val drawTopLeft = Offset(cellTopLeft.x + pad, cellTopLeft.y + pad)
                            val drawSize = Size(cellSize - pad * 2, cellSize - pad * 2)

                            if (isBlasting) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = drawTopLeft,
                                    size = drawSize,
                                    cornerRadius = cellRadius
                                )
                            } else {
                                BlockDrawing.drawBlock(
                                    drawScope = this,
                                    topLeft = drawTopLeft,
                                    size = drawSize,
                                    colorIndex = colorIdx,
                                    theme = theme
                                )
                            }
                        }
                    }
                }
            }

            // 3. Draw ghost preview of dragging piece
            if (dragState.isDragging && dragState.canPlace && dragState.piece != null &&
                dragState.hoverRow != null && dragState.hoverCol != null
            ) {
                val piece = dragState.piece
                val startR = dragState.hoverRow
                val startC = dragState.hoverCol

                val ghostTL = Offset(startC * cellSize, startR * cellSize)
                BlockDrawing.drawPiece(
                    drawScope = this,
                    piece = piece,
                    topLeft = ghostTL,
                    cellSize = cellSize,
                    theme = theme,
                    isGhost = true
                )
            }

            // 4. Draw blast particles
            for (p in gameState.particles) {
                drawCircle(
                    color = p.color,
                    radius = p.radius,
                    center = Offset(p.x, p.y)
                )
            }
        }

        // 5. Floating Score Popups
        for (popup in gameState.scorePopups) {
            FloatingScoreText(
                text = popup.text,
                color = popup.color,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun FloatingScoreText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1.2f, tween(300))
        alpha.animateTo(0f, tween(400))
    }

    Text(
        text = text,
        color = color.copy(alpha = alpha.value),
        fontSize = (26 * scale.value).sp,
        fontWeight = FontWeight.Black,
        modifier = modifier
    )
}
