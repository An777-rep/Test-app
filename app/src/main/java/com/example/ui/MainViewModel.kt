package com.example.ui

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HostEntity
import com.example.data.NetworkUtils
import com.example.remote.WindowsCompanionScript
import com.example.server.LocalWebServer
import com.example.server.SharedFile
import com.example.server.WebServerLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScanResult(
    val ip: String,
    val openPorts: List<Pair<Int, String>>,
    val isPingable: Boolean
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val hostDao = db.hostDao()
    val webServer = LocalWebServer(application)

    val savedHosts: StateFlow<List<HostEntity>> = hostDao.getAllHosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _deviceIp = MutableStateFlow("127.0.0.1")
    val deviceIp: StateFlow<String> = _deviceIp.asStateFlow()

    private val _isWifiConnected = MutableStateFlow(false)
    val isWifiConnected: StateFlow<Boolean> = _isWifiConnected.asStateFlow()

    val isServerRunning: StateFlow<Boolean> = webServer.serverState
    val serverLogs: StateFlow<List<WebServerLog>> = webServer.logs
    val sharedClipboard: StateFlow<String> = webServer.sharedClipboard
    val sharedFiles: StateFlow<List<SharedFile>> = webServer.sharedFiles

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _scanProgress = MutableStateFlow(false)
    val scanProgress: StateFlow<Boolean> = _scanProgress.asStateFlow()

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults.asStateFlow()

    private val _wolLogs = MutableStateFlow<List<String>>(emptyList())
    val wolLogs: StateFlow<List<String>> = _wolLogs.asStateFlow()

    init {
        refreshNetworkInfo()
        // Default auto-start server for immediate ease of use
        toggleWebServer(8080)
    }

    fun refreshNetworkInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val ip = NetworkUtils.getLocalIpAddress(getApplication())
            val wifi = NetworkUtils.isWifiConnected(getApplication())
            _deviceIp.value = ip
            _isWifiConnected.value = wifi
        }
    }

    fun toggleWebServer(port: Int = 8080) {
        if (webServer.serverState.value) {
            webServer.stopServer()
            _statusMessage.value = "Веб-сервер остановлен"
        } else {
            val result = webServer.startServer(port)
            if (result.isSuccess) {
                _statusMessage.value = "Веб-сервер запущен на порту $port"
            } else {
                _statusMessage.value = "Ошибка запуска сервера: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateClipboardFromAndroid(text: String) {
        webServer.updateClipboardText(text, "Android Phone")
        // Also update system clipboard
        try {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("WinLink", text)
            clipboard.setPrimaryClip(clip)
            _statusMessage.value = "Скопировано в буфер обмена Android"
        } catch (e: Exception) {
            Log.e("MainViewModel", "Clipboard error", e)
        }
    }

    fun sendWakeOnLanPacket(macAddress: String, broadcastIp: String = "255.255.255.255", port: Int = 9) {
        viewModelScope.launch {
            val res = NetworkUtils.sendWakeOnLan(macAddress, broadcastIp, port)
            val msg = res.getOrElse { it.localizedMessage ?: "Ошибка отправки WoL" }
            _statusMessage.value = msg
            _wolLogs.value = listOf("[${System.currentTimeMillis()}] $msg") + _wolLogs.value.take(20)
        }
    }

    fun saveHost(name: String, ip: String, mac: String, port: Int = 8080) {
        viewModelScope.launch {
            hostDao.insertHost(
                HostEntity(
                    name = name.ifBlank { "Windows PC" },
                    ipAddress = ip,
                    macAddress = mac,
                    port = port
                )
            )
            _statusMessage.value = "ПК '$name' сохранен"
        }
    }

    fun deleteHost(host: HostEntity) {
        viewModelScope.launch {
            hostDao.deleteHost(host)
            _statusMessage.value = "ПК '${host.name}' удален"
        }
    }

    fun scanNetworkRange(targetIpOrSubnet: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _scanProgress.value = true
            _scanResults.value = emptyList()

            val results = mutableListOf<ScanResult>()
            val baseIp = if (targetIpOrSubnet.endsWith(".x") || targetIpOrSubnet.contains("/")) {
                targetIpOrSubnet.substringBeforeLast(".")
            } else if (targetIpOrSubnet.count { it == '.' } == 3) {
                targetIpOrSubnet
            } else {
                _deviceIp.value.substringBeforeLast(".")
            }

            if (baseIp.count { it == '.' } == 3) {
                // Single host scan
                val isPing = NetworkUtils.pingHost(baseIp)
                val openPorts = NetworkUtils.scanCommonPorts(baseIp)
                results.add(ScanResult(baseIp, openPorts, isPing))
            } else {
                // Sweep subnet e.g. 192.168.1.1 to 192.168.1.30 for fast UI feedback
                for (i in 1..35) {
                    val currentIp = "$baseIp.$i"
                    if (currentIp == _deviceIp.value) continue
                    val isPing = NetworkUtils.pingHost(currentIp, 200)
                    if (isPing) {
                        val openPorts = NetworkUtils.scanCommonPorts(currentIp)
                        results.add(ScanResult(currentIp, openPorts, true))
                    }
                }
            }

            _scanResults.value = results
            _scanProgress.value = false
            _statusMessage.value = "Сканирование завершено. Найдено узлов: ${results.size}"
        }
    }

    fun sendRemoteCommand(ip: String, command: String) {
        viewModelScope.launch {
            val res = WindowsCompanionScript.sendCommandToWindows(ip, 9090, command)
            if (res.isFailure) {
                _statusMessage.value = "Ошибка связи с Windows (порт 9090): ${res.exceptionOrNull()?.localizedMessage}. Убедитесь, что запустили скрипт на ПК!"
            } else {
                _statusMessage.value = "Отправлено: $command"
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
