package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.HostEntity
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
fun SavedHostsScreen(
    viewModel: MainViewModel,
    onNavigateToRemote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val savedHosts by viewModel.savedHosts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var inputName by remember { mutableStateOf("") }
    var inputIp by remember { mutableStateOf("") }
    var inputMac by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = WinCyanSecondary,
                contentColor = WinTextPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("add_host_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить ПК")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "💻 Сохраненные компьютеры Windows",
                    style = MaterialTheme.typography.titleLarge,
                    color = WinTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Управляйте списком ваших рабочих столов и ноутбуков в сети",
                    style = MaterialTheme.typography.bodySmall,
                    color = WinTextSecondary
                )
            }

            if (savedHosts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = WinCardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WinBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = WinTextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Список ПК пуст",
                                style = MaterialTheme.typography.titleMedium,
                                color = WinTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Нажмите кнопкой '+' внизу, чтобы добавить ваш компьютер Windows по IP и MAC-адресу",
                                style = MaterialTheme.typography.bodySmall,
                                color = WinTextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                items(savedHosts, key = { it.id }) { host ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("host_card_${host.id}"),
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
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(WinCyanSecondary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Computer,
                                            contentDescription = null,
                                            tint = WinCyanPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = host.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = WinTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "IP: ${host.ipAddress}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WinCyanPrimary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (host.macAddress.isNotBlank()) {
                                            Text(
                                                text = "MAC: ${host.macAddress}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WinTextSecondary,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteHost(host) },
                                    modifier = Modifier.testTag("delete_host_${host.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = WinAccentRed.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToRemote(host.ipAddress) },
                                    colors = ButtonDefaults.buttonColors(containerColor = WinCyanSecondary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("remote_control_btn_${host.id}")
                                ) {
                                    Icon(Icons.Default.Mouse, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Пульт ДУ", fontSize = 12.sp)
                                }

                                if (host.macAddress.isNotBlank()) {
                                    Button(
                                        onClick = { viewModel.sendWakeOnLanPacket(host.macAddress) },
                                        colors = ButtonDefaults.buttonColors(containerColor = WinAccentGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("wol_btn_${host.id}")
                                    ) {
                                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("WoL Включить", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = WinCardBg,
            title = {
                Text(
                    text = "Добавить ПК Windows",
                    color = WinTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Имя ПК (например: Домашний ПК)") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WinTextPrimary,
                            unfocusedTextColor = WinTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = inputIp,
                        onValueChange = { inputIp = it },
                        label = { Text("IP-адрес в LAN (например 192.168.1.10)") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_ip_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WinTextPrimary,
                            unfocusedTextColor = WinTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = inputMac,
                        onValueChange = { inputMac = it },
                        label = { Text("MAC-адрес для WoL (необязательно)") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_mac_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WinTextPrimary,
                            unfocusedTextColor = WinTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputIp.isNotBlank()) {
                            viewModel.saveHost(inputName, inputIp, inputMac)
                            inputName = ""
                            inputIp = ""
                            inputMac = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WinCyanSecondary),
                    modifier = Modifier.testTag("dialog_save_btn")
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена", color = WinTextSecondary)
                }
            }
        )
    }
}
