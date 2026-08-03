package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections

object NetworkUtils {

    fun getLocalIpAddress(context: Context): String {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipInt = wifiInfo.ipAddress
            if (ipInt != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: continue
                        if (sAddr.indexOf(':') < 0) { // IPv4
                            if (sAddr.startsWith("192.168.") || sAddr.startsWith("10.") || sAddr.startsWith("172.")) {
                                return sAddr
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }

    fun isWifiConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    suspend fun sendWakeOnLan(macAddressRaw: String, broadcastIp: String = "255.255.255.255", port: Int = 9): Result<String> = withContext(Dispatchers.IO) {
        try {
            val macClean = macAddressRaw.replace(":", "").replace("-", "").replace(" ", "")
            if (macClean.length != 12) {
                return@withContext Result.failure(IllegalArgumentException("Неверный формат MAC-адреса (должен содержать 12 символов)"))
            }

            val macBytes = ByteArray(6)
            for (i in 0 until 6) {
                macBytes[i] = macClean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }

            val bytes = ByteArray(6 + 16 * macBytes.size)
            for (i in 0 until 6) {
                bytes[i] = 0xFF.toByte()
            }
            for (i in 6 until bytes.size step macBytes.size) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
            }

            val address = InetAddress.getByName(broadcastIp)
            val packet = DatagramPacket(bytes, bytes.size, address, port)
            val socket = DatagramSocket()
            socket.broadcast = true
            socket.send(packet)
            socket.close()

            Result.success("Wake-on-LAN пакет отправлен на $macAddressRaw ($broadcastIp:$port)")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pingHost(ipAddress: String, timeoutMs: Int = 1000): Boolean = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(ipAddress)
            if (address.isReachable(timeoutMs)) {
                return@withContext true
            }
            // Fallback: try socket connection to common ports
            for (port in listOf(135, 445, 80, 3389, 8080)) {
                if (isPortOpen(ipAddress, port, 300)) return@withContext true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isPortOpen(ipAddress: String, port: Int, timeoutMs: Int = 500): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(ipAddress, port), timeoutMs)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun scanCommonPorts(ipAddress: String): List<Pair<Int, String>> = withContext(Dispatchers.IO) {
        val commonPorts = listOf(
            21 to "FTP",
            22 to "SSH",
            80 to "HTTP Web",
            135 to "RPC",
            139 to "NetBIOS",
            445 to "SMB (Windows Share)",
            3389 to "RDP (Удаленный рабочий стол)",
            5985 to "WinRM",
            8080 to "HTTP Alternate",
            9090 to "WinLink Companion Agent"
        )
        val openPorts = mutableListOf<Pair<Int, String>>()
        for ((port, desc) in commonPorts) {
            if (isPortOpen(ipAddress, port, 400)) {
                openPorts.add(port to desc)
            }
        }
        openPorts
    }
}
