package com.diewland.wifihotspot101

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object WifiHotspot {

    private const val TAG = "WIFI_HOTSPOT"

    // "? (192.168.43.242) at 84:98:66:99:46:95 [ether]  on wlan0? (192.168.43.222) at 76:24:7a:b0:58:c5 [ether]  on wlan0"
    fun getClients(): ArrayList<HotspotClient> {
        val clients = arrayListOf<HotspotClient>()
        val (out, err) = exec("busybox arp -a")

        // cannot list
        if (out == null) {
            Log.e(TAG, "list clients failed: $err")
            return clients
        }
        // extract output to data class
        out.split("?").forEach {
            if (it.isEmpty()) return@forEach
            val patt = Regex("\\((.+)\\) at (.+) \\[.+ on (.+)")
            val m = patt.find(it.trim()) ?: return@forEach
            val (ip, mac, interfaze) = m.destructured
            clients.add(HotspotClient(ip, mac, interfaze))
        }
        return clients
    }

    fun getClientsNoCache(): ArrayList<HotspotClient> {
        getClients().forEach { exec("ping -c 1 -W 1 ${it.ip}") } // clear cache
        return getClients()
    }

    // private methods

    private fun exec (cmd: String): ArrayList<String?> {
        val proc = Runtime.getRuntime().exec(cmd)
        val output = extractOutput(proc)
        proc.waitFor()
        return output
    }

    private fun extractOutput (proc: Process): ArrayList<String?> {
        // gather resp
        val stdInput = BufferedReader(InputStreamReader(proc.inputStream))
        val stdError = BufferedReader(InputStreamReader(proc.errorStream))
        var s: String? // null included
        var o: String? = ""
        var e: String? = ""

        // read counter
        var numX = 10 // max
        var numO = 0
        var numE = 0

        while ((stdInput.readLine().also { s = it } != null) && (numO < numX)) { o += s; numO += 1 }
        while ((stdError.readLine().also { s = it } != null) && (numE < numX)) { e += s; numE += 1 }

        // if blank, cast to null
        if (o.isNullOrBlank()) o = null
        if (e.isNullOrBlank()) e = null

        // debug
        // Log.d(TAG, "[success $numO] $o")
        // Log.d(TAG, "[error   $numE] $e")

        // return output, error
        return arrayListOf(o, e)
    }

}

data class HotspotClient(
    val ip: String,
    val mac: String,
    val interfaze: String,
)