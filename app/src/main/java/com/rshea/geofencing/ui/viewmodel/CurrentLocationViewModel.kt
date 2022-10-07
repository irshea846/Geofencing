package com.rshea.geofencing.ui.viewmodel

import com.rshea.geofencing.ui.uistate.CurrentLocationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrentLocationViewModel {
    private val _uiState = MutableStateFlow(CurrentLocationUiState())
    val uiState: StateFlow<CurrentLocationUiState> = _uiState.asStateFlow()
}