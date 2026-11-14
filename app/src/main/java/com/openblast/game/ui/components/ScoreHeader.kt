package com.openblast.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openblast.game.model.GameState

@Composable
fun ScoreHeader(
    gameState: GameState,
    onOpenThemes: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenSettings: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = gameState.theme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top action bar (Themes, Trophy / Best, Info, Settings, Restart)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme button
            HeaderIconButton(
                text = "🎨",
                tooltip = "Темы",
                onClick = onOpenThemes,
                backgroundColor = theme.boardBackground,
                borderColor = theme.boardBorderColor
            )

            // Best score pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.boardBackground)
                    .border(1.dp, theme.boardBorderColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "👑 ", fontSize = 16.sp)
                Text(
                    text = "РЕКОРД ",
                    color = theme.textSecondaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${gameState.bestScore}",
                    color = theme.accentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Right icons: Info + Settings + Restart
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderIconButton(
                    text = "ℹ️",
                    tooltip = "Инфо",
                    onClick = onOpenInfo,
                    backgroundColor = theme.boardBackground,
                    borderColor = theme.boardBorderColor
                )

                HeaderIconButton(
                    text = "⚙️",
                    tooltip = "Настройки",
                    onClick = onOpenSettings,
                    backgroundColor = theme.boardBackground,
                    borderColor = theme.boardBorderColor
                )

                HeaderIconButton(
                    text = "🔄",
                    tooltip = "Заново",
                    onClick = onRestart,
                    backgroundColor = theme.boardBackground,
                    borderColor = theme.boardBorderColor
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Score display
        Text(
            text = "${gameState.score}",
            color = theme.textColor,
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )

        // Combo badge if active
        AnimatedVisibility(
            visible = gameState.comboCount > 1,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.accentColor)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🔥 COMBO x${gameState.comboCount}!",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    text: String,
    tooltip: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    borderColor: Color
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 18.sp)
    }
}
