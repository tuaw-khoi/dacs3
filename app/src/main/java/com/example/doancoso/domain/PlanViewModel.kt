package com.example.doancoso.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancoso.data.models.DestinationDetails
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.data.repository.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty

interface BasePlan

sealed class PlanUiState {
    object Idle : PlanUiState()
    object Loading : PlanUiState()
    object FetchingPlans : PlanUiState()
    data class Success(val plan: BasePlan) : PlanUiState()
    data class SuccessMultiple(val plans: List<PlanResult>) : PlanUiState()
    data class Error(val message: String) : PlanUiState()
}

sealed class DestinationUiState {
    object Idle : DestinationUiState()
    object Loading : DestinationUiState()
    data class Success(val destination: DestinationDetails) : DestinationUiState()
    data class Error(val message: String) : DestinationUiState()
}

class PlanViewModel(
    private val planRepository: PlanRepository,
) : ViewModel() {

    private val _planState = MutableStateFlow<PlanUiState>(PlanUiState.Idle)
    val planState: StateFlow<PlanUiState> = _planState

    private val _destinationState = MutableStateFlow<DestinationUiState>(DestinationUiState.Idle)
    val destinationState: StateFlow<DestinationUiState> = _destinationState

    fun fetchPlans(destination: String, startDate: String, endDate: String) {
        _planState.value = PlanUiState.Loading

        viewModelScope.launch {
            try {
                val plan = planRepository.getPlan(destination, startDate, endDate)
                _planState.value = PlanUiState.Success(plan)
            } catch (e: Exception) {
                _planState.value = PlanUiState.Error(e.message ?: "Có lỗi xảy ra")
            }
        }
    }


    fun fetchDestinationDetails(destination: String) {
        _destinationState.value = DestinationUiState.Loading

        viewModelScope.launch {
            try {
                val destinationDetails = planRepository.getDestinationDetails(destination)
                _destinationState.value = DestinationUiState.Success(destinationDetails)
            } catch (e: Exception) {
                _destinationState.value = DestinationUiState.Error(e.message ?: "Có lỗi xảy ra khi lấy thông tin điểm đến")
            }
        }
    }

    fun savePlanToFirebase(uid: String, plan: PlanResult, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            planRepository.savePlan(uid, plan, onComplete)
        }
    }

    fun fetchPlansFromFirebase(uid: String) {
        _planState.value = PlanUiState.FetchingPlans

        viewModelScope.launch {
            try {
                val result = planRepository.getPlansFromDb(uid)
                if (result.isSuccess) {
                    val plans = result.getOrNull() ?: emptyList()
                    if (plans.isNotEmpty()) {
                        _planState.value = PlanUiState.SuccessMultiple(plans)
                    } else {
                        _planState.value = PlanUiState.Error("Không có kế hoạch nào.")
                    }
                } else {
                    _planState.value = PlanUiState.Error("Lỗi khi lấy kế hoạch từ DB")
                }
            } catch (e: Exception) {
                _planState.value = PlanUiState.Error(e.message ?: "Có lỗi xảy ra")
            }
        }
    }

    fun fetchPlanByIdFromFirebase(uid: String, planId: String) {
        _planState.value = PlanUiState.Loading

        viewModelScope.launch {
            try {
                val result = planRepository.getPlanById(uid, planId)
                if (result.isSuccess) {
                    val plan = result.getOrNull()
                    if (plan != null) {
                        _planState.value = PlanUiState.Success(plan)
                    } else {
                        _planState.value = PlanUiState.Error("Kế hoạch không tồn tại.")
                    }
                } else {
                    _planState.value = PlanUiState.Error("Lỗi khi lấy kế hoạch từ DB")
                }
            } catch (e: Exception) {
                _planState.value = PlanUiState.Error(e.message ?: "Có lỗi xảy ra")
            }
        }
    }


    fun resetState() {
        _planState.value = PlanUiState.Idle
    }
}


