package com.openblast.game.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openblast.game.model.GameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOverDialog(
    gameState: GameState,
    onRestart: () -> Unit
) {
    if (!gameState.isGameOver) return

    val theme = gameState.theme
    val isNewRecord = gameState.score > 0 && gameState.score >= gameState.bestScore

    BasicAlertDialog(
        onDismissRequest = { /* Game over requires action */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(theme.boardBackground)
                .border(2.dp, theme.boardBorderColor, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "👾",
                    fontSize = 44.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "НЕТ ХОДОВ",
                    color = theme.textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                if (isNewRecord) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🏆 НОВЫЙ РЕКОРД! 🏆",
                        color = theme.accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Score pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.cellEmptyColor)
                        .border(1.dp, theme.cellEmptyBorderColor, RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "СЧЁТ",
                        color = theme.textSecondaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${gameState.score}",
                        color = theme.textColor,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Лучший: ",
                            color = theme.textSecondaryColor,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${gameState.bestScore}",
                            color = theme.accentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Restart button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.accentColor)
                        .clickable(onClick = onRestart),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ИГРАТЬ СНОВА",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
