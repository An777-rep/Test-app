package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.QRCodeVisualizer
import com.example.ui.theme.WinAccentGreen
import com.example.ui.theme.WinAccentRed
import com.example.ui.theme.WinBorderColor
import com.example.ui.theme.WinCardBg
import com.example.ui.theme.WinCyanPrimary
import com.example.ui.theme.WinCyanSecondary
import com.example.ui.theme.WinTextPrimary
import com.example.ui.theme.WinTextSecondary

@Composable
fun WebHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val deviceIp by viewModel.deviceIp.collectAsState()
    val isWifiConnected by viewModel.isWifiConnected.collectAsState()
    val isServerRunning by viewModel.isServerRunning.collectAsState()
    val logs by viewModel.serverLogs.collectAsState()
    val clipboardText by viewModel.sharedClipboard.collectAsState()
    val sharedFiles by viewModel.sharedFiles.collectAsState()
    val context = LocalContext.current

    var clipInputText by remember(clipboardText) { mutableStateOf(clipboardText) }
    var showQrDialog by remember { mutableStateOf(false) }

    val serverUrl = "http://$deviceIp:${viewModel.webServer.port}"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Network Status Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("network_status_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WinCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(WinCyanSecondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = "Wi-Fi Status",
                                    tint = WinCyanPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isWifiConnected) "Локальная сеть Wi-Fi" else "Нет Wi-Fi подключения",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = WinTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "IP телефона: $deviceIp",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WinCyanPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.refreshNetworkInfo() },
                            modifier = Modifier.testTag("refresh_network_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Обновить сеть",
                                tint = WinTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Web Server Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isServerRunning) WinAccentGreen else WinAccentRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isServerRunning) "Веб-сервер АКТИВЕН" else "Веб-сервер ОСТАНОВЛЕН",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isServerRunning) WinAccentGreen else WinTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = isServerRunning,
                            onCheckedChange = { viewModel.toggleWebServer(8080) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WinTextPrimary,
                                checkedTrackColor = WinCyanSecondary,
                                uncheckedThumbColor = WinTextSecondary,
                                uncheckedTrackColor = WinBorderColor
                            ),
                            modifier = Modifier.testTag("web_server_switch")
                        )
                    }

                    // Server Address Details
                    AnimatedVisibility(visible = isServerRunning) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Откройте этот адрес в браузере Windows:",
                                style = MaterialTheme.typography.bodySmall,
                                color = WinTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = serverUrl,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = WinCyanPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        viewModel.updateClipboardFromAndroid(serverUrl)
                                    },
                                    modifier = Modifier.testTag("copy_url_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Копировать URL",
                                        tint = WinCyanPrimary
                                    )
                                }

                                IconButton(
                                    onClick = { showQrDialog = !showQrDialog },
                                    modifier = Modifier.testTag("qr_code_toggle_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "QR-код",
                                        tint = WinCyanPrimary
                                    )
                                }
                            }

                            if (showQrDialog) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    QRCodeVisualizer(text = serverUrl)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Сканируйте QR-код для быстрого перехода с ПК",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WinTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Shared Clipboard Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shared_clipboard_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WinCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "📋 Общий буфер обмена (Android ↔ Windows)",
                        style = MaterialTheme.typography.titleMedium,
                        color = WinTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Текст мгновенно синхронизируется между ПК и телефоном через сеть.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WinTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = clipInputText,
                        onValueChange = { clipInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clipboard_input_field"),
                        placeholder = { Text("Введите текст для передачи на Windows...") },
                        maxLines = 4,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateClipboardFromAndroid(clipInputText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WinCyanSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("send_clipboard_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Обновить и Скопировать")
                        }
                    }
                }
            }
        }

        // Received Shared Files Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shared_files_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WinCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = WinCyanPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📁 Общие файлы (${sharedFiles.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = WinTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { viewModel.webServer.refreshFilesList() },
                            modifier = Modifier.testTag("refresh_files_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Обновить файлы",
                                tint = WinTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (sharedFiles.isEmpty()) {
                        Text(
                            text = "Файлов пока нет. Загрузите файлы через веб-панель на Windows!",
                            style = MaterialTheme.typography.bodySmall,
                            color = WinTextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            sharedFiles.take(5).forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = WinTextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${file.sizeBytes / 1024} КБ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WinTextSecondary
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Файл готов",
                                        tint = WinAccentGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Server Activity Logs Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("server_logs_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WinCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "⚡ Живые логи подключений (Windows)",
                        style = MaterialTheme.typography.titleMedium,
                        color = WinTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (logs.isEmpty()) {
                        Text(
                            text = "Ожидание подключений с локальной сети...",
                            style = MaterialTheme.typography.bodySmall,
                            color = WinTextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            logs.take(6).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = log.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WinCyanPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "[${log.clientIp}]",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WinTextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${log.action}: ${log.details}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WinTextPrimary,
                                        modifier = Modifier.weight(1f)
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
