package com.xmbest.demo.ui.screen.amap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xmbest.demo.data.repository.AmapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 高德广播功能的ViewModel
 */
@HiltViewModel
class AmapViewModel @Inject constructor(
    private val amapRepository: AmapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AmapUiState())
    val uiState: StateFlow<AmapUiState> = _uiState.asStateFlow()

    init {
        startListeningToBroadcast()
    }

    /**
     * 处理UI事件
     */
    fun onEvent(event: AmapUiEvent) {
        when (event) {
            is AmapUiEvent.NavigateToHome -> {
                amapRepository.navigateToHomeOrCompany(true)
            }

            is AmapUiEvent.NavigateToCompany -> {
                amapRepository.navigateToHomeOrCompany(false)
            }

            is AmapUiEvent.NavigateToPlace -> {
                amapRepository.navigateToPlace(event.keyword)
            }

            is AmapUiEvent.BroadcastReceived -> {
                addBroadcastData(event.broadcastData)
            }
        }
    }

    /**
     * 开始监听广播
     */
    private fun startListeningToBroadcast() {
        viewModelScope.launch {
            amapRepository.listenToBroadcast()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
                .collect { broadcastData ->
                    onEvent(AmapUiEvent.BroadcastReceived(broadcastData))
                }
        }
    }

    /**
     * 添加广播数据
     */
    private fun addBroadcastData(data: String) {
        _uiState.value = _uiState.value.copy(
            broadcastDataList = _uiState.value.broadcastDataList + data
        )
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}