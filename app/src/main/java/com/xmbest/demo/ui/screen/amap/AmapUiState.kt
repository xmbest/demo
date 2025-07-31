package com.xmbest.demo.ui.screen.amap

/**
 * 高德广播功能的UI状态
 */
data class AmapUiState(
    val broadcastDataList: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)