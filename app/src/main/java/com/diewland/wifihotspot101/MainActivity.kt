package com.diewland.wifihotspot101

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

const val TAG = "WIFI_HOTSPOT"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clients = WifiHotspot.getClientsNoCache()
        clients.forEach { Log.d(TAG, it.toString()) }

        val output = findViewById<TextView>(R.id.tv_output)
        output.text = clients.joinToString("\n") { "$it" }

    }
}