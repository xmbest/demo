package com.xmbest.demo.data.repository

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 高德广播功能的Repository
 */
@Singleton
class AmapRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // 高德发送的广播，我方接收
        private const val ACTION_RECV = "AUTONAVI_STANDARD_BROADCAST_SEND"
        // 高德接收的广播，我方发送
        private const val ACTION_SEND = "AUTONAVI_STANDARD_BROADCAST_RECV"
        private const val TAG = "AmapRepository"
    }

    /**
     * 监听高德广播
     */
    fun listenToBroadcast(): Flow<String> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "onReceive")
                intent?.let {
                    val dataLines = mutableListOf<String>()
                    dataLines.add("action: ${it.action ?: "unknown"}")
                    it.extras?.let { bundle ->
                        for (key in bundle.keySet()) {
                            val value = bundle.get(key)?.toString() ?: "null"
                            dataLines.add("$key: $value")
                        }
                    }
                    trySend(dataLines.joinToString("\n"))
                }
            }
        }

        @SuppressLint("all")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(ACTION_RECV), RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(ACTION_RECV))
        }

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    /**
     * 导航到家或公司
     */
    fun navigateToHomeOrCompany(toHome: Boolean) {
        val intent = Intent(ACTION_SEND)
        intent.putExtra("KEY_TYPE", 10040)
        intent.putExtra("DEST", if (toHome) 0 else 1)
        context.sendBroadcast(intent)
    }

    /**
     * 导航到指定地点
     */
    fun navigateToPlace(keyword: String) {
        val intent = Intent(ACTION_SEND)
        intent.putExtra("KEY_TYPE", 10023)
        intent.putExtra("EXTRA_SEARCHTYPE", keyword)
        intent.putExtra("EXTRA_DEV", 0)
        context.sendBroadcast(intent)
    }
}