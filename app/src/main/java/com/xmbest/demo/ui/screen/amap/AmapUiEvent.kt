package com.xmbest.demo.ui.screen.amap

/**
 * 高德广播功能的UI事件
 */
sealed class AmapUiEvent {
    object NavigateToHome : AmapUiEvent()
    object NavigateToCompany : AmapUiEvent()
    data class NavigateToPlace(val keyword: String) : AmapUiEvent()
    data class BroadcastReceived(val broadcastData: String) : AmapUiEvent()
}