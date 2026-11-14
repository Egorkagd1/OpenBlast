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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.openblast.game.model.ThemeConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    theme: ThemeConfig,
    isSoundEnabled: Boolean,
    isHapticsEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(theme.boardBackground)
                .border(2.dp, theme.boardBorderColor, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "НАСТРОЙКИ ⚙️",
                        color = theme.textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.boardBorderColor.copy(alpha = 0.5f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = theme.textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sound Toggle
                SettingsItem(
                    icon = "🔊",
                    title = "Звуковые эффекты",
                    subtitle = "Щелчки блоков и комбо",
                    checked = isSoundEnabled,
                    onCheckedChange = onToggleSound,
                    theme = theme
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Haptics Toggle
                SettingsItem(
                    icon = "📳",
                    title = "Вибрация",
                    subtitle = "Тактильный отклик при взрывах",
                    checked = isHapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    theme = theme
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Footer version
                Text(
                    text = "OpenBlast • Material 3 & Jetpack Compose",
                    color = theme.textSecondaryColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    theme: ThemeConfig
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(theme.cellEmptyColor)
            .border(1.dp, theme.cellEmptyBorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = icon, fontSize = 22.sp)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    color = theme.textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = theme.textSecondaryColor,
                    fontSize = 12.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = theme.textColor,
                checkedTrackColor = theme.accentColor,
                uncheckedThumbColor = theme.textSecondaryColor,
                uncheckedTrackColor = theme.cellEmptyBorderColor
            )
        )
    }
}
