package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.WinAccentGreen
import com.example.ui.theme.WinAccentRed
import com.example.ui.theme.WinBorderColor
import com.example.ui.theme.WinCardBg
import com.example.ui.theme.WinCyanPrimary
import com.example.ui.theme.WinCyanSecondary
import com.example.ui.theme.WinTextPrimary
import com.example.ui.theme.WinTextSecondary

@Composable
fun WakeOnLanScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val deviceIp by viewModel.deviceIp.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanResults by viewModel.scanResults.collectAsState()
    val wolLogs by viewModel.wolLogs.collectAsState()

    var macInput by remember { mutableStateOf("00:11:22:33:44:55") }
    var broadcastIpInput by remember { mutableStateOf("255.255.255.255") }
    var targetScanIp by remember { mutableStateOf(deviceIp.substringBeforeLast(".") + ".100") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Wake on LAN Magic Packet Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wol_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WinCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WinAccentGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = WinAccentGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Wake-on-LAN (Включение ПК)",
                                style = MaterialTheme.typography.titleMedium,
                                color = WinTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Отправка Magic Packet для включения ПК по сети",
                                style = MaterialTheme.typography.bodySmall,
                                color = WinTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = macInput,
                        onValueChange = { macInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mac_address_input"),
                        label = { Text("MAC-адрес ПК (например: 00:11:22:33:44:55)") },
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

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = broadcastIpInput,
                        onValueChange = { broadcastIpInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("broadcast_ip_input"),
                        label = { Text("Broadcast IP") },
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.sendWakeOnLanPacket(macInput, broadcastIpInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WinAccentGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_wol_btn")
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Отправить Magic Packet (Включить ПК)", fontWeight = FontWeight.Bold)
                    }

                    if (wolLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Лог отправки:",
                            style = MaterialTheme.typography.labelSmall,
                            color = WinTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        wolLogs.take(3).forEach { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.labelSmall,
                                color = WinCyanPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Network Port Scanner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("port_scanner_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WinCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = WinCyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔍 Сканер сети и портов Windows",
                            style = MaterialTheme.typography.titleMedium,
                            color = WinTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = targetScanIp,
                        onValueChange = { targetScanIp = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_ip_input"),
                        label = { Text("IP узел для сканирования (например 192.168.1.100)") },
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

                    Button(
                        onClick = {
                            viewModel.scanNetworkRange(targetScanIp)
                        },
                        enabled = !scanProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = WinCyanSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_scan_btn")
                    ) {
                        if (scanProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = WinTextPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Сканирование...")
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Проверить доступность и порты")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (scanResults.isNotEmpty()) {
                        Text(
                            text = "Результаты сканирования:",
                            style = MaterialTheme.typography.labelLarge,
                            color = WinTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        scanResults.forEach { res ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "IP: ${res.ip}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = WinCyanPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (res.isPingable) WinAccentGreen else WinAccentRed)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (res.isPingable) "PING OK" else "НЕ ОТВЕЧАЕТ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (res.isPingable) WinAccentGreen else WinAccentRed
                                        )
                                    }
                                }

                                if (res.openPorts.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Открытые порты:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WinTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    res.openPorts.forEach { (port, desc) ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = WinAccentGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Порт $port ($desc)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = WinTextPrimary,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Открытых стандартных портов не обнаружено",
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
    }
}
