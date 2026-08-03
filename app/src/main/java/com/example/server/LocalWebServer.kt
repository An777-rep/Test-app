package com.example.server

import android.content.Context
import android.os.BatteryManager
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WebServerLog(
    val timestamp: String,
    val clientIp: String,
    val action: String,
    val details: String
)

data class SharedFile(
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val path: String
)

class LocalWebServer(private val context: Context) {

    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _serverState = MutableStateFlow(false)
    val serverState: StateFlow<Boolean> = _serverState.asStateFlow()

    private val _logs = MutableStateFlow<List<WebServerLog>>(emptyList())
    val logs: StateFlow<List<WebServerLog>> = _logs.asStateFlow()

    private val _sharedClipboard = MutableStateFlow("Готово к обмену текстом между Windows и Android")
    val sharedClipboard: StateFlow<String> = _sharedClipboard.asStateFlow()

    private val _sharedFiles = MutableStateFlow<List<SharedFile>>(emptyList())
    val sharedFiles: StateFlow<List<SharedFile>> = _sharedFiles.asStateFlow()

    var port: Int = 8080

    init {
        refreshFilesList()
    }

    private fun getStorageDir(): File {
        val dir = File(context.filesDir, "lan_shared")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun refreshFilesList() {
        val dir = getStorageDir()
        val files = dir.listFiles()?.map { file ->
            SharedFile(
                name = file.name,
                sizeBytes = file.length(),
                lastModified = file.lastModified(),
                path = file.absolutePath
            )
        }?.sortedByDescending { it.lastModified } ?: emptyList()
        _sharedFiles.value = files
    }

    fun updateClipboardText(text: String, source: String = "Android") {
        _sharedClipboard.value = text
        addLog(source, "Буфер обмена", "Обновлен текст: ${text.take(30)}...")
    }

    fun startServer(serverPort: Int = 8080): Result<Boolean> {
        if (isRunning) return Result.success(true)
        this.port = serverPort

        return try {
            serverSocket = ServerSocket(port)
            isRunning = true
            _serverState.value = true

            addLog("Система", "Запуск сервера", "Веб-сервер запущен на порту $port")

            serverJob = scope.launch {
                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        handleClient(clientSocket)
                    } catch (e: Exception) {
                        if (!isRunning) break
                        Log.e("LocalWebServer", "Error accepting client", e)
                    }
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            isRunning = false
            _serverState.value = false
            Result.failure(e)
        }
    }

    fun stopServer() {
        isRunning = false
        _serverState.value = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serverJob?.cancel()
        addLog("Система", "Останов", "Веб-сервер остановлен")
    }

    private fun addLog(ip: String, action: String, details: String) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newLog = WebServerLog(timeStr, ip, action, details)
        scope.launch(Dispatchers.Main) {
            _logs.value = (listOf(newLog) + _logs.value).take(100)
        }
    }

    private fun handleClient(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            try {
                val clientIp = socket.inetAddress.hostAddress ?: "Unknown"
                val input = socket.getInputStream()
                val reader = BufferedReader(InputStreamReader(input))
                val requestLine = reader.readLine() ?: return@launch

                val tokens = requestLine.split(" ")
                if (tokens.size < 2) return@launch
                val method = tokens[0]
                val uri = tokens[1]

                var contentLength = 0
                var contentType = ""
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line.isNullOrEmpty()) break
                    val lower = line!!.lowercase(Locale.getDefault())
                    if (lower.startsWith("content-length:")) {
                        contentLength = line!!.substring(15).trim().toIntOrNull() ?: 0
                    }
                    if (lower.startsWith("content-type:")) {
                        contentType = line!!.substring(13).trim()
                    }
                }

                val output = socket.getOutputStream()

                when {
                    uri == "/" || uri == "/index.html" -> {
                        addLog(clientIp, "Запрос страницы", "Windows открыл Веб-панель")
                        sendHtmlResponse(output, generateWebDashboardHtml())
                    }
                    uri == "/api/status" -> {
                        val statusJson = getDeviceStatusJson()
                        sendJsonResponse(output, statusJson)
                    }
                    uri == "/api/clipboard" && method == "GET" -> {
                        val text = _sharedClipboard.value
                        val json = "{\"text\":\"${escapeJson(text)}\"}"
                        sendJsonResponse(output, json)
                    }
                    uri.startsWith("/api/clipboard") && method == "POST" -> {
                        val body = CharArray(contentLength)
                        reader.read(body, 0, contentLength)
                        val bodyStr = String(body)
                        val textToCopy = if (bodyStr.startsWith("text=")) {
                            URLDecoder.decode(bodyStr.substring(5), "UTF-8")
                        } else bodyStr

                        updateClipboardText(textToCopy, clientIp)
                        sendJsonResponse(output, "{\"status\":\"ok\"}")
                    }
                    uri == "/api/files" && method == "GET" -> {
                        val filesList = _sharedFiles.value.joinToString(",") {
                            "{\"name\":\"${escapeJson(it.name)}\",\"size\":${it.sizeBytes},\"path\":\"${escapeJson(it.path)}\"}"
                        }
                        sendJsonResponse(output, "[$filesList]")
                    }
                    uri.startsWith("/api/download") && method == "GET" -> {
                        val fileName = uri.substringAfter("name=", "").let { URLDecoder.decode(it, "UTF-8") }
                        val file = File(getStorageDir(), fileName)
                        if (file.exists() && file.isFile) {
                            addLog(clientIp, "Скачивание", "Скачан файл: ${file.name}")
                            sendFileResponse(output, file)
                        } else {
                            sendResponse(output, "404 Not Found", "text/plain", "Файл не найден")
                        }
                    }
                    uri.startsWith("/api/upload") && method == "POST" -> {
                        addLog(clientIp, "Загрузка файла", "Обработка загрузки файла с Windows")
                        saveUploadedFile(input, contentLength, contentType)
                        refreshFilesList()
                        sendJsonResponse(output, "{\"status\":\"success\",\"message\":\"Файл успешно получен на телефон\"}")
                    }
                    uri.startsWith("/api/notify") -> {
                        val msg = uri.substringAfter("msg=", "Привет с Windows!").let { URLDecoder.decode(it, "UTF-8") }
                        addLog(clientIp, "Уведомление", "Сообщение с PC: $msg")
                        sendJsonResponse(output, "{\"status\":\"ok\"}")
                    }
                    else -> {
                        sendResponse(output, "404 Not Found", "text/plain", "Endpoint not found")
                    }
                }
            } catch (e: Exception) {
                Log.e("LocalWebServer", "Client handling error", e)
            } finally {
                try {
                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun getDeviceStatusJson(): String {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val model = android.os.Build.MODEL
        val manufacturer = android.os.Build.MANUFACTURER
        val androidVer = android.os.Build.VERSION.RELEASE

        val stat = Environment.getDataDirectory()
        val availBytes = stat.freeSpace
        val availMb = availBytes / (1024 * 1024)

        return """
            {
                "model": "$manufacturer $model",
                "androidVersion": "$androidVer",
                "battery": $batteryLevel,
                "freeStorageMb": $availMb,
                "serverPort": $port
            }
        """.trimIndent()
    }

    private fun saveUploadedFile(input: java.io.InputStream, contentLength: Int, contentType: String) {
        val destFile = File(getStorageDir(), "win_upload_${System.currentTimeMillis()}.bin")
        FileOutputStream(destFile).use { fos ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0
            while (totalRead < contentLength) {
                val toRead = minOf(buffer.size, contentLength - totalRead)
                bytesRead = input.read(buffer, 0, toRead)
                if (bytesRead == -1) break
                fos.write(buffer, 0, bytesRead)
                totalRead += bytesRead
            }
        }
        addLog("Windows PC", "Сохранен файл", destFile.name)
    }

    private fun sendHtmlResponse(out: OutputStream, html: String) {
        sendResponse(out, "200 OK", "text/html; charset=utf-8", html)
    }

    private fun sendJsonResponse(out: OutputStream, json: String) {
        sendResponse(out, "200 OK", "application/json; charset=utf-8", json)
    }

    private fun sendResponse(out: OutputStream, status: String, contentType: String, content: String) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 $status\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(bytes)
        out.flush()
    }

    private fun sendFileResponse(out: OutputStream, file: File) {
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "Content-Disposition: attachment; filename=\"${file.name}\"\r\n" +
                "Content-Length: ${file.length()}\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))

        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
        }
        out.flush()
    }

    private fun generateWebDashboardHtml(): String {
        return """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>WinLink LAN Companion</title>
                <style>
                    :root {
                        --bg: #0f172a;
                        --card: #1e293b;
                        --accent: #38bdf8;
                        --text: #f8fafc;
                        --text-sec: #94a3b8;
                    }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: var(--bg);
                        color: var(--text);
                        margin: 0;
                        padding: 24px;
                        display: flex;
                        justify-content: center;
                    }
                    .container {
                        max-width: 800px;
                        width: 100%;
                    }
                    .header {
                        display: flex;
                        align-items: center;
                        gap: 16px;
                        border-bottom: 2px solid #334155;
                        padding-bottom: 16px;
                        margin-bottom: 24px;
                    }
                    .icon {
                        width: 48px;
                        height: 48px;
                        background: linear-gradient(135deg, #0284c7, #38bdf8);
                        border-radius: 12px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 24px;
                        font-weight: bold;
                    }
                    h1 { margin: 0; font-size: 24px; }
                    .subtitle { color: var(--text-sec); font-size: 14px; }
                    .card {
                        background: var(--card);
                        border-radius: 16px;
                        padding: 20px;
                        margin-bottom: 20px;
                        box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
                        border: 1px solid #334155;
                    }
                    .card-title {
                        font-size: 18px;
                        font-weight: 600;
                        margin-bottom: 12px;
                        color: var(--accent);
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    }
                    textarea {
                        width: 100%;
                        height: 90px;
                        background: #0f172a;
                        border: 1px solid #475569;
                        border-radius: 8px;
                        color: #f8fafc;
                        padding: 12px;
                        box-sizing: border-box;
                        font-family: inherit;
                        resize: vertical;
                    }
                    .btn-row {
                        display: flex;
                        gap: 12px;
                        margin-top: 12px;
                    }
                    button {
                        background: #0284c7;
                        color: white;
                        border: none;
                        padding: 10px 20px;
                        border-radius: 8px;
                        font-weight: 600;
                        cursor: pointer;
                        transition: 0.2s;
                    }
                    button:hover { background: #0369a1; }
                    button.secondary { background: #334155; }
                    button.secondary:hover { background: #475569; }
                    .file-list {
                        list-style: none;
                        padding: 0;
                        margin: 12px 0 0 0;
                    }
                    .file-item {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        background: #0f172a;
                        padding: 10px 14px;
                        border-radius: 8px;
                        margin-bottom: 8px;
                    }
                    .badge {
                        background: #0284c7;
                        padding: 2px 8px;
                        border-radius: 12px;
                        font-size: 12px;
                    }
                    .drop-zone {
                        border: 2px dashed #38bdf8;
                        border-radius: 12px;
                        padding: 24px;
                        text-align: center;
                        color: var(--text-sec);
                        cursor: pointer;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="icon">💻</div>
                        <div>
                            <h1>WinLink LAN — Windows Control Panel</h1>
                            <div class="subtitle">Локальное подключение с Android устройством</div>
                        </div>
                    </div>

                    <!-- Status Card -->
                    <div class="card">
                        <div class="card-title">📱 Статус смартфона</div>
                        <div id="deviceInfo">Загрузка информации об устройстве...</div>
                    </div>

                    <!-- Clipboard Card -->
                    <div class="card">
                        <div class="card-title">📋 Общий буфер обмена</div>
                        <textarea id="clipText" placeholder="Введите текст для отправки на телефон..."></textarea>
                        <div class="btn-row">
                            <button onclick="sendClipboard()">Отправить на телефон</button>
                            <button class="secondary" onclick="fetchClipboard()">Обновить с телефона</button>
                        </div>
                    </div>

                    <!-- File Upload & Share -->
                    <div class="card">
                        <div class="card-title">📁 Передача файлов (Windows ↔ Phone)</div>
                        <input type="file" id="fileInput" style="display:none" onchange="uploadFile()">
                        <div class="drop-zone" onclick="document.getElementById('fileInput').click()">
                            📄 Нажмите здесь, чтобы выбрать файл для отправки на телефон
                        </div>
                        <div style="margin-top: 16px;">
                            <strong>Файлы на телефоне:</strong>
                            <ul class="file-list" id="fileList">
                                <li>Загрузка списка файлов...</li>
                            </ul>
                        </div>
                    </div>
                    
                    <!-- Quick Notification -->
                    <div class="card">
                        <div class="card-title">🔔 Сообщение на экран телефона</div>
                        <input type="text" id="notifText" placeholder="Привет с ПК!" style="width: 100%; padding: 10px; background: #0f172a; border: 1px solid #475569; color: white; border-radius: 8px; box-sizing: border-box;">
                        <div class="btn-row">
                            <button onclick="sendNotify()">Показать всплывающее уведомление</button>
                        </div>
                    </div>
                </div>

                <script>
                    async function fetchStatus() {
                        try {
                            const res = await fetch('/api/status');
                            const data = await res.json();
                            document.getElementById('deviceInfo').innerHTML = 
                                `Модель: <b>${'$'}{data.model}</b> | Android: <b>${'$'}{data.androidVersion}</b> | Заряд: <b>${'$'}{data.battery}%</b> | Свободно: <b>${'$'}{data.freeStorageMb} МБ</b>`;
                        } catch(e) {
                            document.getElementById('deviceInfo').innerText = 'Ошибка соединения с телефоном';
                        }
                    }

                    async function fetchClipboard() {
                        try {
                            const res = await fetch('/api/clipboard');
                            const data = await res.json();
                            document.getElementById('clipText').value = data.text;
                        } catch(e) {}
                    }

                    async function sendClipboard() {
                        const val = document.getElementById('clipText').value;
                        await fetch('/api/clipboard', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                            body: 'text=' + encodeURIComponent(val)
                        });
                        alert('Текст отправлен на телефон!');
                    }

                    async function fetchFiles() {
                        try {
                            const res = await fetch('/api/files');
                            const files = await res.json();
                            const ul = document.getElementById('fileList');
                            if(files.length === 0) {
                                ul.innerHTML = '<li>Файлов пока нет</li>';
                                return;
                            }
                            ul.innerHTML = files.map(f => `
                                <li class="file-item">
                                    <span>📄 <b>${'$'}{f.name}</b> (${'$'}{(f.size/1024).toFixed(1)} КБ)</span>
                                    <a href="/api/download?name=${'$'}{encodeURIComponent(f.name)}" download><button>Скачать</button></a>
                                </li>
                            `).join('');
                        } catch(e) {}
                    }

                    async function uploadFile() {
                        const input = document.getElementById('fileInput');
                        if (!input.files || input.files.length === 0) return;
                        const file = input.files[0];
                        
                        const res = await fetch('/api/upload', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/octet-stream'},
                            body: file
                        });
                        alert('Файл ' + file.name + ' отправлен!');
                        fetchFiles();
                    }

                    async function sendNotify() {
                        const txt = document.getElementById('notifText').value || 'Привет!';
                        await fetch('/api/notify?msg=' + encodeURIComponent(txt));
                        alert('Уведомление отправлено!');
                    }

                    fetchStatus();
                    fetchClipboard();
                    fetchFiles();
                    setInterval(fetchStatus, 5000);
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
