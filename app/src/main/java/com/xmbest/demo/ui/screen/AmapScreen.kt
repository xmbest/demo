package com.xmbest.demo.ui.screen

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * 高德广播功能验证
 */

// 高德发送的广播，我方接收
private const val ACTION_RECV = "AUTONAVI_STANDARD_BROADCAST_SEND"

// 高德接收的广播，我方发送
private const val ACTION_SEND = "AUTONAVI_STANDARD_BROADCAST_RECV"

private const val TAG = "AmapScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AmapScreen(modifier: Modifier) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val intents = MutableSharedFlow<Intent>()
    var broadcastDataList by remember { mutableStateOf<List<String>>(emptyList()) }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive")
            intent?.let {
                coroutineScope.launch {
                    intents.emit(it)
                }
            }
        }

    }

    fun registerBroadCast(context: Context) {
        @SuppressLint("all")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(ACTION_RECV), RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(ACTION_RECV))
        }
    }

    LaunchedEffect(Unit) {
        registerBroadCast(context)
        coroutineScope.launch {
            intents.collect { intent ->
                val dataLines = mutableListOf<String>()
                dataLines.add("action: ${intent.action ?: "unknown"}")
                intent.extras?.let { bundle ->
                    for (key in bundle.keySet()) {
                        val value = bundle.get(key)?.toString() ?: "null"
                        dataLines.add("$key: $value")
                    }
                }
                broadcastDataList = broadcastDataList + dataLines.joinToString("\n")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("高德广播验证") },
            )
        }, snackbarHost = { SnackbarHost(snackbarHostState) }, modifier = modifier
    ) { paddingValues ->
        Column(
            modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 导航按钮区域
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        toHomeOrCompony(context, true)
                    }
                ) {
                    Text("导航回家")
                }

                Button(
                    onClick = {
                        toHomeOrCompony(context, false)
                    }
                ) {
                    Text("导航去公司")
                }

                Button(
                    onClick = {
                        to(context, "世界之窗")
                    }
                ) {
                    Text("导航去")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 广播数据显示卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "接收到的广播数据",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (broadcastDataList.isEmpty()) {
                        Text(
                            text = "暂无广播数据",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn {
                            items(broadcastDataList) { dataLine ->
                                Text(
                                    text = dataLine,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun toHomeOrCompony(context: Context, toHome: Boolean) {
    val intent = Intent(ACTION_SEND)
    intent.putExtra("KEY_TYPE", 10040)
    intent.putExtra("DEST", if (toHome) 0 else 1)
    context.sendBroadcast(intent)
}


private fun to(context: Context, keyword: String) {
    val intent = Intent(ACTION_SEND)
    intent.putExtra("KEY_TYPE", 10023)
    intent.putExtra("EXTRA_SEARCHTYPE", keyword)
    intent.putExtra("EXTRA_DEV", 0)
    context.sendBroadcast(intent)
}
