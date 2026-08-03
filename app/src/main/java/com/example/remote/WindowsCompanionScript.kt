package com.example.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

object WindowsCompanionScript {

    suspend fun sendCommandToWindows(ip: String, port: Int = 9090, command: String, timeoutMs: Int = 1000): Result<String> = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println(command)
                Result.success("Команда '$command' отправлена на $ip:$port")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPowerShellScriptText(): String {
        val d = '$'
        return """
# WinLink Companion — Легкий PowerShell агент для Windows (Не требует установки)
# Сохраните как winlink_agent.ps1 и запустите через PowerShell

Add-Type -TypeDefinition @"
using System;
using System.Runtime.InteropServices;

public class WinAPI {
    [DllImport("user32.dll")]
    public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, int dwExtraInfo);
    
    [DllImport("user32.dll")]
    public static extern bool LockWorkStation();

    [DllImport("user32.dll")]
    public static extern void mouse_event(uint dwFlags, int dx, int dy, uint dwData, int dwExtraInfo);

    public const uint MOUSEEVENTF_MOVE = 0x0001;
    public const uint MOUSEEVENTF_LEFTDOWN = 0x0002;
    public const uint MOUSEEVENTF_LEFTUP = 0x0004;
    public const uint MOUSEEVENTF_RIGHTDOWN = 0x0008;
    public const uint MOUSEEVENTF_RIGHTUP = 0x0010;

    public const byte VK_VOLUME_MUTE = 0xAD;
    public const byte VK_VOLUME_DOWN = 0xAE;
    public const byte VK_VOLUME_UP = 0xAF;
    public const byte VK_MEDIA_NEXT_TRACK = 0xB0;
    public const byte VK_MEDIA_PREV_TRACK = 0xB1;
    public const byte VK_MEDIA_PLAY_PAUSE = 0xB3;
}
"@

${d}listener = New-Object System.Net.Sockets.TcpListener([System.Net.IPAddress]::Any, 9090)
${d}listener.Start()
Write-Host "==========================================" -ForegroundColor Green
Write-Host " WinLink Agent запущен на порту 9090!" -ForegroundColor Cyan
Write-Host " Ожидание команд с Android..." -ForegroundColor Yellow
Write-Host "=========================================="

while (${d}true) {
    try {
        ${d}client = ${d}listener.AcceptTcpClient()
        ${d}stream = ${d}client.GetStream()
        ${d}reader = New-Object System.IO.StreamReader(${d}stream)
        ${d}cmd = ${d}reader.ReadLine()
        
        Write-Host "Получена команда: ${d}cmd" -ForegroundColor Gray
        
        switch -Wildcard (${d}cmd) {
            "VOL_UP" { [WinAPI]::keybd_event([WinAPI]::VK_VOLUME_UP, 0, 0, 0); [WinAPI]::keybd_event([WinAPI]::VK_VOLUME_UP, 0, 2, 0) }
            "VOL_DOWN" { [WinAPI]::keybd_event([WinAPI]::VK_VOLUME_DOWN, 0, 0, 0); [WinAPI]::keybd_event([WinAPI]::VK_VOLUME_DOWN, 0, 2, 0) }
            "MUTE" { [WinAPI]::keybd_event([WinAPI]::VK_VOLUME_MUTE, 0, 0, 0); [WinAPI]::keybd_event([WinAPI]::VK_VOLUME_MUTE, 0, 2, 0) }
            "MEDIA_PLAY" { [WinAPI]::keybd_event([WinAPI]::VK_MEDIA_PLAY_PAUSE, 0, 0, 0); [WinAPI]::keybd_event([WinAPI]::VK_MEDIA_PLAY_PAUSE, 0, 2, 0) }
            "MEDIA_NEXT" { [WinAPI]::keybd_event([WinAPI]::VK_MEDIA_NEXT_TRACK, 0, 0, 0); [WinAPI]::keybd_event([WinAPI]::VK_MEDIA_NEXT_TRACK, 0, 2, 0) }
            "MEDIA_PREV" { [WinAPI]::keybd_event([WinAPI]::VK_MEDIA_PREV_TRACK, 0, 0, 0); [WinAPI]::keybd_event([WinAPI]::VK_MEDIA_PREV_TRACK, 0, 2, 0) }
            "LOCK" { [WinAPI]::LockWorkStation() }
            "SLEEP" { rundll32.exe powrprof.dll,SetSuspendState 0,1,0 }
            "SHUTDOWN" { shutdown /s /t 60 }
            "CLICK_LEFT" { [WinAPI]::mouse_event([WinAPI]::MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0); [WinAPI]::mouse_event([WinAPI]::MOUSEEVENTF_LEFTUP, 0, 0, 0, 0) }
            "CLICK_RIGHT" { [WinAPI]::mouse_event([WinAPI]::MOUSEEVENTF_RIGHTDOWN, 0, 0, 0, 0); [WinAPI]::mouse_event([WinAPI]::MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0) }
            "MOVE *" {
                ${d}parts = ${d}cmd.Split(' ')
                ${d}dx = [int]${d}parts[1]
                ${d}dy = [int]${d}parts[2]
                [WinAPI]::mouse_event([WinAPI]::MOUSEEVENTF_MOVE, ${d}dx, ${d}dy, 0, 0)
            }
        }
        ${d}client.Close()
    } catch {
        # ignore single error
    }
}
        """.trimIndent()
    }
}
