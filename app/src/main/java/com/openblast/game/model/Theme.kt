package com.openblast.game.model

import android.content.Context
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class BlockStyleType {
    FLAT_ROUNDED,
    TILKAJO,
    SAUSAGE,
    MATERIAL
}

data class ThemeConfig(
    val id: String,
    val name: String,
    val description: String,
    val styleType: BlockStyleType,
    val backgroundBrush: Brush,
    val boardBackground: Color,
    val boardBorderColor: Color,
    val cellEmptyColor: Color,
    val cellEmptyBorderColor: Color,
    val textColor: Color,
    val textSecondaryColor: Color,
    val accentColor: Color,
    val pieceColors: List<Color>,
    val pieceGradients: List<Pair<Color, Color>>,
    val particleColors: List<Color>
)

object OpenBlastThemes {
    // 1. Classic Block Blast Vibrant
    val CLASSIC = ThemeConfig(
        id = "classic",
        name = "Классика",
        description = "Сочные неоновые цвета оригинального Block Blast",
        styleType = BlockStyleType.FLAT_ROUNDED,
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
        ),
        boardBackground = Color(0xFF131A2E),
        boardBorderColor = Color(0xFF263353),
        cellEmptyColor = Color(0xFF1E2843),
        cellEmptyBorderColor = Color(0xFF2B385D),
        textColor = Color(0xFFFFFFFF),
        textSecondaryColor = Color(0xFF94A3B8),
        accentColor = Color(0xFFFFB703),
        pieceColors = listOf(
            Color(0xFF3B82F6), // Blue
            Color(0xFFEF4444), // Red
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Amber
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFF06B6D4)  // Cyan
        ),
        pieceGradients = listOf(
            Color(0xFF60A5FA) to Color(0xFF2563EB),
            Color(0xFFF87171) to Color(0xFFDC2626),
            Color(0xFF34D399) to Color(0xFF059669),
            Color(0xFFFBBF24) to Color(0xFFD97706),
            Color(0xFFA78BFA) to Color(0xFF7C3AED),
            Color(0xFFF472B6) to Color(0xFFDB2777),
            Color(0xFF22D3EE) to Color(0xFF0891B2)
        ),
        particleColors = listOf(
            Color(0xFFFFD166), Color(0xFF06D6A0), Color(0xFF118AB2), Color(0xFFEF476F)
        )
    )

    // 2. Material You 3 (Dynamic Monet Palette from Android System)
    val MATERIAL_YOU = ThemeConfig(
        id = "material_you",
        name = "Material You ✨",
        description = "Динамические цвета Monet из обоев твоего телефона",
        styleType = BlockStyleType.MATERIAL,
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1A1C1E), Color(0xFF22272E))
        ),
        boardBackground = Color(0xFF242A32),
        boardBorderColor = Color(0xFF39414E),
        cellEmptyColor = Color(0xFF2D353F),
        cellEmptyBorderColor = Color(0xFF3B4452),
        textColor = Color(0xFFE2E2E6),
        textSecondaryColor = Color(0xFF9EA3AF),
        accentColor = Color(0xFF9ECBF9),
        pieceColors = listOf(
            Color(0xFF9ECBF9),
            Color(0xFFFFB4AB),
            Color(0xFFA6D39D),
            Color(0xFFFFDDAE),
            Color(0xFFD2BCFF),
            Color(0xFFBCEBEB)
        ),
        pieceGradients = listOf(
            Color(0xFFB8DAFF) to Color(0xFF81B4E8),
            Color(0xFFFFCCC4) to Color(0xFFE8988D),
            Color(0xFFC0E5B8) to Color(0xFF8DBF84),
            Color(0xFFFFE8C7) to Color(0xFFE4C391),
            Color(0xFFE3D4FF) to Color(0xFFB698EE),
            Color(0xFFD4F6F6) to Color(0xFFA2D7D7)
        ),
        particleColors = listOf(
            Color(0xFF9ECBF9), Color(0xFFA6D39D), Color(0xFFFFB4AB), Color(0xFFD2BCFF)
        )
    )

    fun createDynamicMaterialYou(context: Context): ThemeConfig {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val darkScheme = dynamicDarkColorScheme(context)
                val p = darkScheme.primary
                val s = darkScheme.secondary
                val t = darkScheme.tertiary
                val pc = darkScheme.primaryContainer
                val sc = darkScheme.secondaryContainer
                val tc = darkScheme.tertiaryContainer

                return ThemeConfig(
                    id = "material_you",
                    name = "Material You ✨",
                    description = "Родные цвета Monet из обоев твоего телефона",
                    styleType = BlockStyleType.MATERIAL,
                    backgroundBrush = Brush.verticalGradient(
                        colors = listOf(darkScheme.background, darkScheme.surface)
                    ),
                    boardBackground = darkScheme.surfaceVariant.copy(alpha = 0.65f),
                    boardBorderColor = darkScheme.outlineVariant,
                    cellEmptyColor = darkScheme.surfaceVariant.copy(alpha = 0.35f),
                    cellEmptyBorderColor = darkScheme.outline.copy(alpha = 0.25f),
                    textColor = darkScheme.onSurface,
                    textSecondaryColor = darkScheme.onSurfaceVariant,
                    accentColor = darkScheme.primary,
                    pieceColors = listOf(p, s, t, pc, sc, tc),
                    pieceGradients = listOf(
                        p to darkScheme.onPrimaryContainer,
                        s to darkScheme.onSecondaryContainer,
                        t to darkScheme.onTertiaryContainer,
                        pc to p,
                        sc to s,
                        tc to t
                    ),
                    particleColors = listOf(p, s, t, pc)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return MATERIAL_YOU
    }

    // 3. Тилькайо (Tilkajo / Stretched Fat Round Tilcayo Cat)
    val TILKAJO = ThemeConfig(
        id = "tilkajo",
        name = "Тилькайо 🐱",
        description = "Растянутый жирный круглый котик тилькайо со щёчками",
        styleType = BlockStyleType.TILKAJO,
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF2E1A11), Color(0xFF190C06))
        ),
        boardBackground = Color(0xFF24140D),
        boardBorderColor = Color(0xFFE5A65E),
        cellEmptyColor = Color(0xFF361E13),
        cellEmptyBorderColor = Color(0xFF4D2B1C),
        textColor = Color(0xFFFFE8D6),
        textSecondaryColor = Color(0xFFDDB892),
        accentColor = Color(0xFFFFB703),
        pieceColors = listOf(
            Color(0xFFE09F3E), // Рыжий круглый тилькайо
            Color(0xFF9E7651), // Пятнистый пухлый тилькайо
            Color(0xFFD4A373), // Песочный жирный тилькайо
            Color(0xFFC8553D), // Шальной тилькайо
            Color(0xFF588157), // Лесной круглый котяра
            Color(0xFF7F5539)  // Сердитый толстый тилькайо
        ),
        pieceGradients = listOf(
            Color(0xFFFFBF69) to Color(0xFFCB852A),
            Color(0xFFB58E68) to Color(0xFF875E39),
            Color(0xFFE9C49E) to Color(0xFFB88351),
            Color(0xFFE2735B) to Color(0xFFA83B24),
            Color(0xFF739E72) to Color(0xFF426841),
            Color(0xFF9A6E50) to Color(0xFF673F26)
        ),
        particleColors = listOf(
            Color(0xFFFFB703), Color(0xFFFFE8D6), Color(0xFFE09F3E), Color(0xFFFF9F1C)
        )
    )

    // 4. Колбасы (Sausages Deluxe 🌭🥩)
    val SAUSAGES = ThemeConfig(
        id = "sausages",
        name = "Колбасы 🌭",
        description = "Докторская, салями, сервелат, краковская и сосиски с горчицей",
        styleType = BlockStyleType.SAUSAGE,
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF2C1608), Color(0xFF421C0E))
        ),
        boardBackground = Color(0xFF381A0E),
        boardBorderColor = Color(0xFF5E2B18),
        cellEmptyColor = Color(0xFF4A2515),
        cellEmptyBorderColor = Color(0xFF61331F),
        textColor = Color(0xFFFFDEB4),
        textSecondaryColor = Color(0xFFE2A478),
        accentColor = Color(0xFFFFC107), // Mustard
        pieceColors = listOf(
            Color(0xFFF4A8A8), // Нежная Докторская вареная
            Color(0xFF9E2A2B), // Салями сухой вялки со шпиком
            Color(0xFFB5452D), // Краковская полукопченая
            Color(0xFFC76D49), // Молочные сосиски
            Color(0xFF781D1D), // Сервелат элитный
            Color(0xFFE5A93C), // Баварские с сыром и горчицей
            Color(0xFFB33939)  // Охотничьи колбаски
        ),
        pieceGradients = listOf(
            Color(0xFFFFBEBE) to Color(0xFFDF8C8C), // Докторская
            Color(0xFFB83B3C) to Color(0xFF7E1B1C), // Салями
            Color(0xFFCF5E44) to Color(0xFF963520), // Краковская
            Color(0xFFDC8461) to Color(0xFFA65331), // Сосиски
            Color(0xFF942929) to Color(0xFF5A1010), // Сервелат
            Color(0xFFF2BC55) to Color(0xFFC88C22), // Горчичные
            Color(0xFFC94A4A) to Color(0xFF902626)  // Охотничьи
        ),
        particleColors = listOf(
            Color(0xFFF4A8A8), Color(0xFFFFC107), Color(0xFF9E2A2B), Color(0xFFFFF0D4)
        )
    )

    val ALL = listOf(CLASSIC, MATERIAL_YOU, TILKAJO, SAUSAGES)

    fun getAll(context: Context? = null): List<ThemeConfig> {
        val matYou = if (context != null) createDynamicMaterialYou(context) else MATERIAL_YOU
        return listOf(CLASSIC, matYou, TILKAJO, SAUSAGES)
    }

    fun getById(id: String, context: Context? = null): ThemeConfig {
        if (id == MATERIAL_YOU.id && context != null) {
            return createDynamicMaterialYou(context)
        }
        return ALL.find { it.id == id } ?: CLASSIC
    }
}
