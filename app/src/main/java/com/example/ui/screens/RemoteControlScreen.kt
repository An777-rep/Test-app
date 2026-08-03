package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remote.WindowsCompanionScript
import com.example.ui.MainViewModel
import com.example.ui.theme.WinAccentOrange
import com.example.ui.theme.WinAccentRed
import com.example.ui.theme.WinBorderColor
import com.example.ui.theme.WinCardBg
import com.example.ui.theme.WinCyanPrimary
import com.example.ui.theme.WinCyanSecondary
import com.example.ui.theme.WinTextPrimary
import com.example.ui.theme.WinTextSecondary

@Composable
fun RemoteControlScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val savedHosts by viewModel.savedHosts.collectAsState()
    val context = LocalContext.current

    var selectedIp by remember { mutableStateOf(savedHosts.firstOrNull()?.ipAddress ?: "192.168.1.100") }
    var selectedTab by remember { mutableStateOf(0) }

    val scriptText = remember { WindowsCompanionScript.getPowerShellScriptText() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Target PC Selector Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("target_pc_selector_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WinCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎯 Целевой ПК Windows",
                        style = MaterialTheme.typography.titleMedium,
                        color = WinTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = selectedIp,
                        onValueChange = { selectedIp = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_ip_input"),
                        label = { Text("IP адрес ПК Windows") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = WinCyanPrimary,
                            unfocusedBorderColor = WinBorderColor,
                            focusedTextColor = WinTextPrimary,
                            unfocusedTextColor = WinTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (savedHosts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Быстрый выбор из сохраненных:",
                            style = MaterialTheme.typography.labelSmall,
                            color = WinTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            savedHosts.take(3).forEach { host ->
                                Button(
                                    onClick = { selectedIp = host.ipAddress },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedIp == host.ipAddress) WinCyanSecondary else MaterialTheme.colorScheme.background
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = host.name,
                                        fontSize = 12.sp,
                                        color = WinTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Tabs: 0 -> Remote Trackpad & Media, 1 -> PowerShell Agent Setup
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = WinCardBg,
                contentColor = WinCyanPrimary,
                edgePadding = 0.dp,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🖱️ Пульт ДУ и Мышь") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("📜 Настройка PowerShell Агента") }
                )
            }
        }

        if (selectedTab == 0) {
            // Trackpad Canvas
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trackpad_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WinCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mouse,
                                    contentDescription = null,
                                    tint = WinCyanPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Сенсорный тачпад (Trackpad)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = WinTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .pointerInput(selectedIp) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val dx = (dragAmount.x * 2.5f).toInt()
                                        val dy = (dragAmount.y * 2.5f).toInt()
                                        viewModel.sendRemoteCommand(selectedIp, "MOVE $dx $dy")
                                    }
                                }
                                .pointerInput(selectedIp) {
                                    detectTapGestures(
                                        onTap = {
                                            viewModel.sendRemoteCommand(selectedIp, "CLICK_LEFT")
                                        },
                                        onLongPress = {
                                            viewModel.sendRemoteCommand(selectedIp, "CLICK_RIGHT")
                                        }
                                    )
                                }
                                .testTag("trackpad_touch_area"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = WinTextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Водите пальцем для управления курсором\nКороткий клик = ЛКМ | Удержание = ПКМ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WinTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Media Remote Controls Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("media_controls_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WinCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎵 Управление медиа и звуком",
                            style = MaterialTheme.typography.titleMedium,
                            color = WinTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = { viewModel.sendRemoteCommand(selectedIp, "VOL_DOWN") },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                                    .testTag("vol_down_btn")
                            ) {
                                Icon(Icons.Default.VolumeDown, contentDescription = "Громкость -", tint = WinCyanPrimary)
                            }

                            IconButton(
                                onClick = { viewModel.sendRemoteCommand(selectedIp, "MUTE") },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                                    .testTag("mute_btn")
                            ) {
                                Icon(Icons.Default.VolumeMute, contentDescription = "Без звука", tint = WinAccentOrange)
                            }

                            IconButton(
                                onClick = { viewModel.sendRemoteCommand(selectedIp, "VOL_UP") },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                                    .testTag("vol_up_btn")
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Громкость +", tint = WinCyanPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = { viewModel.sendRemoteCommand(selectedIp, "MEDIA_PREV") },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                                    .testTag("media_prev_btn")
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Предыдущий трек", tint = WinTextPrimary)
                            }

                            IconButton(
                                onClick = { viewModel.sendRemoteCommand(selectedIp, "MEDIA_PLAY") },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(WinCyanSecondary)
                                    .testTag("media_play_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Воспроизведение/Пауза", tint = WinTextPrimary)
                            }

                            IconButton(
                                onClick = { viewModel.sendRemoteCommand(selectedIp, "MEDIA_NEXT") },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                                    .testTag("media_next_btn")
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Следующий трек", tint = WinTextPrimary)
                            }
                        }
                    }
                }
            }

            // Power & System Actions Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("power_actions_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WinCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚡ Питание и Залочка ПК",
                            style = MaterialTheme.typography.titleMedium,
                            color = WinTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.sendRemoteCommand(selectedIp, "LOCK") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lock_pc_btn")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = WinAccentOrange, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Заблокировать", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.sendRemoteCommand(selectedIp, "SLEEP") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sleep_pc_btn")
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = WinCyanPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Сон", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.sendRemoteCommand(selectedIp, "SHUTDOWN") },
                                colors = ButtonDefaults.buttonColors(containerColor = WinAccentRed.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("shutdown_pc_btn")
                            ) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = WinAccentRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Выключить", fontSize = 12.sp, color = WinAccentRed)
                            }
                        }
                    }
                }
            }
        } else {
            // PowerShell Script Instructions & Copy Code
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("powershell_script_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WinCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = WinCyanPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Инструкция для Windows",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = WinTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("PowerShell Script", scriptText)
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.updateClipboardFromAndroid("PowerShell script copied")
                                },
                                modifier = Modifier.testTag("copy_script_btn")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Скопировать скрипт", tint = WinCyanPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Чтобы управлять громкостью, курсором и питанием ПК без установки программ:\n" +
                                    "1. Скопируйте скрипт ниже.\n" +
                                    "2. На ПК запустите PowerShell и вставьте скрипт.\n" +
                                    "3. Скрипт начнет слушать порт 9090 для приема команд с Android!",
                            style = MaterialTheme.typography.bodySmall,
                            color = WinTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(12.dp)
                        ) {
                            LazyColumn {
                                item {
                                    Text(
                                        text = scriptText,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = WinCyanPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
