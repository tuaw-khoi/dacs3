package com.example.doancoso.domain

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancoso.data.models.ActivityDetailDb
import com.example.doancoso.data.models.DayPlanDb
import com.example.doancoso.data.models.DestinationDetails
import com.example.doancoso.data.models.ItineraryDb
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.data.repository.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
                _destinationState.value = DestinationUiState.Error(
                    e.message ?: "Có lỗi xảy ra khi lấy thông tin điểm đến"
                )
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


    fun updatePlanToFirebase(
        uid: String,
        updatedPlan: PlanResultDb,
        planId: String,
        onSuccess: () -> Unit
    ) {
        _planState.value = PlanUiState.Loading

        viewModelScope.launch {
            try {
                val aiPlan = planRepository.getPlan(
                    updatedPlan.destination,
                    updatedPlan.itinerary.startDate,
                    updatedPlan.itinerary.endDate
                )

                val regeneratedPlan = PlanResultDb(
                    uid = uid,
                    destination = updatedPlan.destination,
                    itinerary = ItineraryDb(
                        destination = aiPlan.destination,
                        startDate = aiPlan.itinerary.startDate,
                        endDate = aiPlan.itinerary.endDate,
                        specialties = aiPlan.itinerary.specialties ?: emptyList(),
                        transportation = aiPlan.itinerary.transportation ?: emptyList(),
                        itinerary = aiPlan.itinerary.itinerary.map { day ->
                            DayPlanDb(
                                activities = day.activities?.map { activity ->
                                    ActivityDetailDb(
                                        description = activity.description,
                                        location = activity.location,
                                        timeOfDay = activity.timeOfDay,
                                        transportation = activity.transportation
                                    )
                                } ?: emptyList()
                            )
                        } ?: emptyList()
                    )
                )

                val result = planRepository.updatePlan(uid, regeneratedPlan, planId)

                result.onSuccess {
                    _planState.value = PlanUiState.Success(regeneratedPlan)
                    onSuccess()
                }.onFailure {
                    _planState.value = PlanUiState.Error("Lỗi khi cập nhật kế hoạch: ${it.localizedMessage}")
                }
            } catch (e: Exception) {
                _planState.value = PlanUiState.Error(e.message ?: "Có lỗi xảy ra khi tạo lại kế hoạch")
            }
        }
    }

    //PlanViewModel
    fun updateDayPlan(uid: String, planId: String, dayIndex: Int, updatedDayPlan: DayPlanDb) {
        _planState.value = PlanUiState.Loading

        viewModelScope.launch {
            try {
                val result = planRepository.updateDayPlan(uid, planId, dayIndex, updatedDayPlan)
                result.onSuccess {

                    fetchPlanByIdFromFirebase(uid, planId)

                    Log.d("PlanViewModel", "Đã cập nhật ngày $dayIndex thành công")
                }.onFailure {
                    _planState.value = PlanUiState.Error("Lỗi khi cập nhật ngày: ${it.localizedMessage}")
                }
            } catch (e: Exception) {
                _planState.value = PlanUiState.Error("Lỗi khi cập nhật ngày: ${e.localizedMessage}")
            }
        }
    }

    fun deleteActivityFromPlan(dayIndex: Int, activityIndex: Int, planId: String, uid: String) {
        _planState.value = PlanUiState.Loading

        viewModelScope.launch {
            try {
                val result = planRepository.deleteActivity(dayIndex, activityIndex, planId, uid)

                result.onSuccess {
                    // Load lại kế hoạch sau khi xóa thành công
                    fetchPlanByIdFromFirebase(uid, planId)
                }.onFailure {
                    _planState.value = PlanUiState.Error("Lỗi khi xóa hoạt động: ${it.localizedMessage}")
                }
            } catch (e: Exception) {
                _planState.value = PlanUiState.Error("Có lỗi xảy ra khi xóa hoạt động: ${e.localizedMessage}")
            }
        }
    }


}


