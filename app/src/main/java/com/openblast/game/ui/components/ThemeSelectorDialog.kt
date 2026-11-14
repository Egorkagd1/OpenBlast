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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.openblast.game.model.OpenBlastThemes
import com.openblast.game.model.ThemeConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorDialog(
    currentTheme: ThemeConfig,
    onSelectTheme: (ThemeConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val allThemes = androidx.compose.runtime.remember(context) { OpenBlastThemes.getAll(context) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(currentTheme.boardBackground)
                .border(2.dp, currentTheme.boardBorderColor, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ВЫБЕРИ ТЕМУ 🎨",
                        color = currentTheme.textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "✕",
                        color = currentTheme.textSecondaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss)
                            .padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allThemes) { theme ->
                        val isSelected = theme.id == currentTheme.id
                        ThemeCard(
                            theme = theme,
                            isSelected = isSelected,
                            currentTheme = currentTheme,
                            onClick = {
                                onSelectTheme(theme)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: ThemeConfig,
    isSelected: Boolean,
    currentTheme: ThemeConfig,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) theme.cellEmptyBorderColor else theme.cellEmptyColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.accentColor else theme.boardBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    color = currentTheme.textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = theme.description,
                    color = currentTheme.textSecondaryColor,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Color palette preview circles
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (color in theme.pieceColors.take(5)) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✓",
                    color = theme.accentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
