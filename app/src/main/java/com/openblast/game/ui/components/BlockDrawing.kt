package com.openblast.game.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.openblast.game.model.BlockStyleType
import com.openblast.game.model.Piece
import com.openblast.game.model.StripInfo
import com.openblast.game.model.ThemeConfig
import kotlin.math.roundToInt

object BlockDrawing {

    var tilkaioBitmap: ImageBitmap? = null

    fun partitionPieceIntoStrips(piece: Piece): List<StripInfo> = piece.partitionIntoStrips()

    fun drawPiece(
        drawScope: DrawScope,
        piece: Piece,
        topLeft: Offset,
        cellSize: Float,
        theme: ThemeConfig,
        alpha: Float = 1f,
        isGhost: Boolean = false
    ) {
        if (theme.styleType == BlockStyleType.TILKAJO && tilkaioBitmap != null) {
            val strips = partitionPieceIntoStrips(piece)
            val bm = tilkaioBitmap!!

            for (strip in strips) {
                val stripTL = Offset(
                    topLeft.x + strip.c * cellSize,
                    topLeft.y + strip.r * cellSize
                )
                val stripW = strip.w * cellSize
                val stripH = strip.h * cellSize

                val stripPath = Path()
                for (sr in 0 until strip.h) {
                    for (sc in 0 until strip.w) {
                        val pad = cellSize * 0.04f
                        val cellTL = Offset(
                            stripTL.x + sc * cellSize + pad,
                            stripTL.y + sr * cellSize + pad
                        )
                        val cellSz = Size(cellSize - pad * 2, cellSize - pad * 2)
                        stripPath.addRoundRect(
                            RoundRect(
                                rect = Rect(cellTL, cellSz),
                                cornerRadius = CornerRadius(cellSize * 0.22f, cellSize * 0.22f)
                            )
                        )
                    }
                }

                drawScope.clipPath(stripPath) {
                    drawScope.drawRect(
                        color = Color(0xFFF4A261).copy(alpha = if (isGhost) 0.2f else 0.85f * alpha),
                        topLeft = stripTL,
                        size = Size(stripW, stripH)
                    )
                    drawScope.drawImage(
                        image = bm,
                        dstOffset = IntOffset(stripTL.x.roundToInt(), stripTL.y.roundToInt()),
                        dstSize = IntSize(stripW.roundToInt(), stripH.roundToInt()),
                        alpha = if (isGhost) 0.35f else alpha
                    )
                }

                for (sr in 0 until strip.h) {
                    for (sc in 0 until strip.w) {
                        val pad = cellSize * 0.04f
                        val cellTL = Offset(
                            stripTL.x + sc * cellSize + pad,
                            stripTL.y + sr * cellSize + pad
                        )
                        val cellSz = Size(cellSize - pad * 2, cellSize - pad * 2)
                        drawScope.drawRoundRect(
                            color = Color(0xFF5E3023).copy(alpha = if (isGhost) 0.25f else 0.45f * alpha),
                            topLeft = cellTL,
                            size = cellSz,
                            cornerRadius = CornerRadius(cellSize * 0.22f, cellSize * 0.22f),
                            style = Stroke(width = cellSize * 0.04f)
                        )
                    }
                }
            }
        } else {
            for (r in 0 until piece.height) {
                for (c in 0 until piece.width) {
                    if (piece[r, c]) {
                        val blockTopLeft = Offset(
                            topLeft.x + c * cellSize,
                            topLeft.y + r * cellSize
                        )
                        drawBlock(
                            drawScope = drawScope,
                            topLeft = blockTopLeft,
                            size = Size(cellSize, cellSize),
                            colorIndex = piece.colorIndex,
                            theme = theme,
                            alpha = alpha,
                            isGhost = isGhost
                        )
                    }
                }
            }
        }
    }

    fun drawBlock(
        drawScope: DrawScope,
        topLeft: Offset,
        size: Size,
        colorIndex: Int,
        theme: ThemeConfig,
        alpha: Float = 1f,
        isGhost: Boolean = false
    ) {
        val safeIndex = colorIndex % theme.pieceColors.size
        val baseColor = theme.pieceColors[safeIndex].copy(alpha = if (isGhost) 0.35f else alpha)
        val gradient = theme.pieceGradients.getOrNull(safeIndex)

        val padding = size.width * 0.04f
        val innerTopLeft = Offset(topLeft.x + padding, topLeft.y + padding)
        val innerSize = Size(size.width - padding * 2, size.height - padding * 2)

        when (theme.styleType) {
            BlockStyleType.TILKAJO -> {
                val bm = tilkaioBitmap
                if (bm != null) {
                    drawScope.drawImage(
                        image = bm,
                        dstOffset = IntOffset(innerTopLeft.x.roundToInt(), innerTopLeft.y.roundToInt()),
                        dstSize = IntSize(innerSize.width.roundToInt(), innerSize.height.roundToInt()),
                        alpha = if (isGhost) 0.35f else alpha
                    )
                } else {
                    drawFatStretchedTilkajo(drawScope, innerTopLeft, innerSize, safeIndex, isGhost)
                }
            }
            BlockStyleType.SAUSAGE -> {
                drawSausageBlock(drawScope, innerTopLeft, innerSize, baseColor, gradient, isGhost)
            }
            BlockStyleType.MATERIAL -> {
                drawMaterialBlock(drawScope, innerTopLeft, innerSize, baseColor, isGhost)
            }
            BlockStyleType.FLAT_ROUNDED -> {
                drawClassicBlock(drawScope, innerTopLeft, innerSize, baseColor, gradient, isGhost)
            }
        }
    }

    private fun drawTilkaioBoardCell(
        drawScope: DrawScope,
        topLeft: Offset,
        size: Size,
        safeIndex: Int,
        isGhost: Boolean,
        alpha: Float
    ) {
        val w = size.width
        val h = size.height
        val cornerRadius = CornerRadius(w * 0.24f, h * 0.24f)

        val plushColor = when (safeIndex % 6) {
            0 -> Color(0xFFF4A261)
            1 -> Color(0xFFE76F51)
            2 -> Color(0xFFDDA15E)
            3 -> Color(0xFFBC6C25)
            4 -> Color(0xFFF2CC8F)
            else -> Color(0xFFE9967A)
        }

        drawScope.drawRoundRect(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(topLeft.x, topLeft.y + h * 0.05f),
            size = size,
            cornerRadius = cornerRadius
        )

        val bodyBrush = Brush.verticalGradient(
            colors = listOf(
                plushColor.copy(alpha = if (isGhost) 0.3f else alpha),
                plushColor.copy(red = (plushColor.red * 0.85f).coerceIn(0f, 1f), alpha = if (isGhost) 0.3f else alpha)
            ),
            startY = topLeft.y,
            endY = topLeft.y + h
        )
        drawScope.drawRoundRect(
            brush = bodyBrush,
            topLeft = topLeft,
            size = size,
            cornerRadius = cornerRadius
        )

        val pawColor = Color(0xFF5E3023).copy(alpha = 0.30f * alpha)
        val cx = topLeft.x + w * 0.5f
        val cy = topLeft.y + h * 0.54f
        drawScope.drawOval(
            color = pawColor,
            topLeft = Offset(cx - w * 0.16f, cy - h * 0.10f),
            size = Size(w * 0.32f, h * 0.22f)
        )
        drawScope.drawCircle(pawColor, w * 0.055f, Offset(cx - w * 0.13f, cy - h * 0.18f))
        drawScope.drawCircle(pawColor, w * 0.06f, Offset(cx, cy - h * 0.22f))
        drawScope.drawCircle(pawColor, w * 0.055f, Offset(cx + w * 0.13f, cy - h * 0.18f))

        drawScope.drawRoundRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = topLeft,
            size = size,
            cornerRadius = cornerRadius,
            style = Stroke(width = w * 0.04f)
        )
    }

    // 1. ЖИРНЫЙ КРУГЛЫЙ РАСТЯНУТЫЙ ТИЛЬКАЙО (Leopardus tilcayo Meme Chonker)
    private fun drawFatStretchedTilkajo(
        drawScope: DrawScope,
        topLeft: Offset,
        size: Size,
        expressionIndex: Int,
        isGhost: Boolean
    ) {
        val w = size.width
        val h = size.height

        if (isGhost) {
            drawScope.drawRoundRect(
                color = Color(0xFFE09F3E).copy(alpha = 0.3f),
                topLeft = topLeft,
                size = size,
                cornerRadius = CornerRadius(w * 0.3f, h * 0.3f)
            )
            return
        }

        // Базовый цвет пушистой шёрстки (плюшевый золотисто-пятнистый окрас)
        val furColor = when (expressionIndex % 6) {
            0 -> Color(0xFFF4A261) // Золотисто-рыжий классик
            1 -> Color(0xFFE76F51) // Шальной терракотовый
            2 -> Color(0xFFDDA15E) // Песочный леопардовый
            3 -> Color(0xFFBC6C25) // Шоколадный пухляш
            4 -> Color(0xFFF2CC8F) // Сливочный булочка
            else -> Color(0xFFE9967A) // Румяный пончик
        }

        val bellyColor = Color(0xFFFFF1E6)
        val earInnerColor = Color(0xFFFFB5A7)

        // 1. Ушки (маленькие округлые плюшевые ушки тилькайо по бокам)
        val earRadius = w * 0.16f
        // Левое ухо
        drawScope.drawCircle(
            color = furColor,
            radius = earRadius,
            center = Offset(topLeft.x + w * 0.18f, topLeft.y + h * 0.15f)
        )
        drawScope.drawCircle(
            color = earInnerColor,
            radius = earRadius * 0.6f,
            center = Offset(topLeft.x + w * 0.18f, topLeft.y + h * 0.15f)
        )
        // Правое ухо
        drawScope.drawCircle(
            color = furColor,
            radius = earRadius,
            center = Offset(topLeft.x + w * 0.82f, topLeft.y + h * 0.15f)
        )
        drawScope.drawCircle(
            color = earInnerColor,
            radius = earRadius * 0.6f,
            center = Offset(topLeft.x + w * 0.82f, topLeft.y + h * 0.15f)
        )

        // 2. Растянутая жирная круглая голова / щёки (Wide Chonker Body)
        // Рисуем сверх-широкий пухлый овал со складками
        val faceTopLeft = Offset(topLeft.x + w * 0.02f, topLeft.y + h * 0.1f)
        val faceSize = Size(w * 0.96f, h * 0.88f)
        drawScope.drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(furColor, furColor.copy(alpha = 0.95f), furColor.copy(red = furColor.red * 0.85f)),
                center = Offset(topLeft.x + w * 0.5f, topLeft.y + h * 0.5f),
                radius = w * 0.65f
            ),
            topLeft = faceTopLeft,
            size = faceSize,
            cornerRadius = CornerRadius(w * 0.42f, h * 0.42f)
        )

        // Пятнышки тилькайо на лбу и щеках (Leopardus spots)
        val spotColor = Color(0xFF5E3023).copy(alpha = 0.65f)
        drawScope.drawCircle(spotColor, w * 0.04f, Offset(topLeft.x + w * 0.5f, topLeft.y + h * 0.22f))
        drawScope.drawCircle(spotColor, w * 0.035f, Offset(topLeft.x + w * 0.4f, topLeft.y + h * 0.25f))
        drawScope.drawCircle(spotColor, w * 0.035f, Offset(topLeft.x + w * 0.6f, topLeft.y + h * 0.25f))
        drawScope.drawCircle(spotColor, w * 0.04f, Offset(topLeft.x + w * 0.12f, topLeft.y + h * 0.5f))
        drawScope.drawCircle(spotColor, w * 0.04f, Offset(topLeft.x + w * 0.88f, topLeft.y + h * 0.5f))

        // 3. Пухлые светлые щёчки и подбородок (Chubby white muzzle)
        val muzzleTopLeft = Offset(topLeft.x + w * 0.2f, topLeft.y + h * 0.52f)
        val muzzleSize = Size(w * 0.6f, h * 0.38f)
        drawScope.drawRoundRect(
            color = bellyColor,
            topLeft = muzzleTopLeft,
            size = muzzleSize,
            cornerRadius = CornerRadius(w * 0.3f, h * 0.3f)
        )

        // 4. Огромные круглые кошачьи глазищи (Big Staring Meme Eyes)
        val eyeRadius = w * 0.15f
        val leftEyeCenter = Offset(topLeft.x + w * 0.28f, topLeft.y + h * 0.42f)
        val rightEyeCenter = Offset(topLeft.x + w * 0.72f, topLeft.y + h * 0.42f)

        when (expressionIndex % 6) {
            1 -> {
                // Шокированный / ошарашенный взгляд O_O
                drawScope.drawCircle(Color.White, eyeRadius * 1.15f, leftEyeCenter)
                drawScope.drawCircle(Color.White, eyeRadius * 1.15f, rightEyeCenter)
                drawScope.drawCircle(Color.Black, eyeRadius * 0.7f, leftEyeCenter)
                drawScope.drawCircle(Color.Black, eyeRadius * 0.7f, rightEyeCenter)
                drawScope.drawCircle(Color.White, eyeRadius * 0.25f, leftEyeCenter - Offset(2f, 2f))
                drawScope.drawCircle(Color.White, eyeRadius * 0.25f, rightEyeCenter - Offset(2f, 2f))
            }
            4 -> {
                // Довольный / прищуренный котяра ^w^
                val strokeW = w * 0.06f
                drawScope.drawArc(
                    color = Color(0xFF3D0C02),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(leftEyeCenter.x - eyeRadius, leftEyeCenter.y - eyeRadius * 0.5f),
                    size = Size(eyeRadius * 2, eyeRadius),
                    style = Stroke(strokeW)
                )
                drawScope.drawArc(
                    color = Color(0xFF3D0C02),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(rightEyeCenter.x - eyeRadius, rightEyeCenter.y - eyeRadius * 0.5f),
                    size = Size(eyeRadius * 2, eyeRadius),
                    style = Stroke(strokeW)
                )
            }
            else -> {
                // Обычные огромные блестящие круглые глаза тилькайо
                val irisColor = Color(0xFF2EC4B6) // Бирюзово-изумрудные
                drawScope.drawCircle(Color.White, eyeRadius, leftEyeCenter)
                drawScope.drawCircle(Color.White, eyeRadius, rightEyeCenter)

                drawScope.drawCircle(irisColor, eyeRadius * 0.85f, leftEyeCenter)
                drawScope.drawCircle(irisColor, eyeRadius * 0.85f, rightEyeCenter)

                // Зрачки
                drawScope.drawCircle(Color.Black, eyeRadius * 0.55f, leftEyeCenter)
                drawScope.drawCircle(Color.Black, eyeRadius * 0.55f, rightEyeCenter)

                // Двойные блики в глазах (милота)
                drawScope.drawCircle(Color.White, eyeRadius * 0.25f, leftEyeCenter - Offset(w * 0.03f, h * 0.03f))
                drawScope.drawCircle(Color.White, eyeRadius * 0.12f, leftEyeCenter + Offset(w * 0.03f, h * 0.03f))
                drawScope.drawCircle(Color.White, eyeRadius * 0.25f, rightEyeCenter - Offset(w * 0.03f, h * 0.03f))
                drawScope.drawCircle(Color.White, eyeRadius * 0.12f, rightEyeCenter + Offset(w * 0.03f, h * 0.03f))
            }
        }

        // 5. Розовый носик
        val noseCenter = Offset(topLeft.x + w * 0.5f, topLeft.y + h * 0.58f)
        drawScope.drawCircle(Color(0xFFFF758F), w * 0.055f, noseCenter)

        // 6. Ротик :3
        val mouthY = topLeft.y + h * 0.67f
        val mouthColor = Color(0xFF5E3023)
        val mStroke = Stroke(w * 0.045f)
        drawScope.drawArc(
            color = mouthColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(topLeft.x + w * 0.38f, mouthY - h * 0.05f),
            size = Size(w * 0.12f, h * 0.1f),
            style = mStroke
        )
        drawScope.drawArc(
            color = mouthColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(topLeft.x + w * 0.5f, mouthY - h * 0.05f),
            size = Size(w * 0.12f, h * 0.1f),
            style = mStroke
        )

        // 7. Усики (растянутые по бокам)
        val whiskerColor = Color(0xFF6B4226)
        val wLen = w * 0.22f
        val whiskerStroke = Stroke(w * 0.03f)
        // Слева
        drawScope.drawLine(whiskerColor, Offset(topLeft.x + w * 0.24f, mouthY), Offset(topLeft.x + w * 0.02f, mouthY - h * 0.04f), strokeWidth = w * 0.03f)
        drawScope.drawLine(whiskerColor, Offset(topLeft.x + w * 0.24f, mouthY + h * 0.04f), Offset(topLeft.x + w * 0.02f, mouthY + h * 0.05f), strokeWidth = w * 0.03f)
        // Справа
        drawScope.drawLine(whiskerColor, Offset(topLeft.x + w * 0.76f, mouthY), Offset(topLeft.x + w * 0.98f, mouthY - h * 0.04f), strokeWidth = w * 0.03f)
        drawScope.drawLine(whiskerColor, Offset(topLeft.x + w * 0.76f, mouthY + h * 0.04f), Offset(topLeft.x + w * 0.98f, mouthY + h * 0.05f), strokeWidth = w * 0.03f)

        // 8. Внешний мягкий контур пухляша
        drawScope.drawRoundRect(
            color = Color(0x33000000),
            topLeft = faceTopLeft,
            size = faceSize,
            cornerRadius = CornerRadius(w * 0.42f, h * 0.42f),
            style = Stroke(w * 0.04f)
        )
    }

    // 3. КОЛБАСЫ (Sausages Deluxe)
    private fun drawSausageBlock(
        drawScope: DrawScope,
        topLeft: Offset,
        size: Size,
        baseColor: Color,
        gradient: Pair<Color, Color>?,
        isGhost: Boolean
    ) {
        val cornerRadius = CornerRadius(size.width * 0.35f, size.height * 0.35f)

        if (gradient != null && !isGhost) {
            val brush = Brush.verticalGradient(
                colors = listOf(gradient.first, gradient.second),
                startY = topLeft.y,
                endY = topLeft.y + size.height
            )
            drawScope.drawRoundRect(
                brush = brush,
                topLeft = topLeft,
                size = size,
                cornerRadius = cornerRadius
            )
        } else {
            drawScope.drawRoundRect(
                color = baseColor,
                topLeft = topLeft,
                size = size,
                cornerRadius = cornerRadius
            )
        }

        if (!isGhost) {
            // Крапинки шпика
            val speckleColor = Color(0xFFFFF0D4).copy(alpha = 0.75f)
            val spRadius = size.width * 0.065f

            val speckles = listOf(
                Offset(topLeft.x + size.width * 0.25f, topLeft.y + size.height * 0.3f),
                Offset(topLeft.x + size.width * 0.65f, topLeft.y + size.height * 0.28f),
                Offset(topLeft.x + size.width * 0.35f, topLeft.y + size.height * 0.7f),
                Offset(topLeft.x + size.width * 0.75f, topLeft.y + size.height * 0.65f),
                Offset(topLeft.x + size.width * 0.5f, topLeft.y + size.height * 0.48f)
            )
            for (pt in speckles) {
                drawScope.drawCircle(
                    color = speckleColor,
                    radius = spRadius,
                    center = pt
                )
            }

            // Натуральная оболочка
            drawScope.drawRoundRect(
                color = Color(0x66000000),
                topLeft = topLeft,
                size = size,
                cornerRadius = cornerRadius,
                style = Stroke(width = size.width * 0.05f)
            )

            // Блик
            drawScope.drawArc(
                color = Color.White.copy(alpha = 0.4f),
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(topLeft.x + size.width * 0.1f, topLeft.y + size.height * 0.1f),
                size = Size(size.width * 0.4f, size.height * 0.4f),
                style = Stroke(width = size.width * 0.08f)
            )
        }
    }

    // 4. КЛАССИКА
    private fun drawClassicBlock(
        drawScope: DrawScope,
        topLeft: Offset,
        size: Size,
        baseColor: Color,
        gradient: Pair<Color, Color>?,
        isGhost: Boolean
    ) {
        val cornerRadius = CornerRadius(size.width * 0.22f, size.height * 0.22f)

        if (gradient != null && !isGhost) {
            val brush = Brush.verticalGradient(
                colors = listOf(gradient.first, gradient.second),
                startY = topLeft.y,
                endY = topLeft.y + size.height
            )
            drawScope.drawRoundRect(
                brush = brush,
                topLeft = topLeft,
                size = size,
                cornerRadius = cornerRadius
            )
        } else {
            drawScope.drawRoundRect(
                color = baseColor,
                topLeft = topLeft,
                size = size,
                cornerRadius = cornerRadius
            )
        }

        if (!isGhost) {
            val highlightBrush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                startY = topLeft.y,
                endY = topLeft.y + size.height * 0.45f
            )
            drawScope.drawRoundRect(
                brush = highlightBrush,
                topLeft = topLeft,
                size = Size(size.width, size.height * 0.45f),
                cornerRadius = cornerRadius
            )

            drawScope.drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                topLeft = topLeft,
                size = size,
                cornerRadius = cornerRadius,
                style = Stroke(width = size.width * 0.05f)
            )
        }
    }

    // 5. MATERIAL YOU
    private fun drawMaterialBlock(
        drawScope: DrawScope,
        topLeft: Offset,
        size: Size,
        baseColor: Color,
        isGhost: Boolean
    ) {
        val cornerRadius = CornerRadius(size.width * 0.28f, size.height * 0.28f)

        drawScope.drawRoundRect(
            color = baseColor,
            topLeft = topLeft,
            size = size,
            cornerRadius = cornerRadius
        )

        if (!isGhost) {
            drawScope.drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = topLeft,
                size = size,
                cornerRadius = cornerRadius,
                style = Stroke(width = size.width * 0.04f)
            )
        }
    }
}
