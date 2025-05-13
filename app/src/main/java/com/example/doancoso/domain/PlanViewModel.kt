package com.example.doancoso.domain

import android.system.Os.link
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.doancoso.data.models.DayPlanDb
import com.example.doancoso.data.models.DestinationDetails
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.data.repository.PlanRepository
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _chatResponse = MutableStateFlow("")
    val chatResponse: StateFlow<String> = _chatResponse

    val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

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
                val result = planRepository.updatePlan(uid, updatedPlan, planId)

                result.onSuccess {
                    _planState.value = PlanUiState.Success(updatedPlan)
                    onSuccess()
                }.onFailure {
                    _planState.value =
                        PlanUiState.Error("Lỗi khi cập nhật kế hoạch: ${it.localizedMessage}")
                }
            } catch (e: Exception) {
                _planState.value =
                    PlanUiState.Error(e.message ?: "Có lỗi xảy ra khi cập nhật kế hoạch")
            }
        }
    }

    //PlanViewModel
    fun updateDayPlan(uid: String, planId: String, dayIndex: Int, updatedDayPlan: DayPlanDb) {
        _planState.value = PlanUiState.Loading

        viewModelScope.launch {
            val result = planRepository.updateDayPlan(uid, planId, dayIndex, updatedDayPlan)
            result.onSuccess {
                fetchPlanByIdFromFirebase(uid, planId)
                Log.d("PlanViewModel", "✅ Day $dayIndex updated successfully")
            }.onFailure {
                _planState.value =
                    PlanUiState.Error("❌ Failed to update day: ${it.localizedMessage}")
            }
        }
    }

    //PlanViewModel
    fun deleteActivityFromPlan(dayIndex: Int, activityIndex: Int, planId: String, uid: String) {
        _planState.value = PlanUiState.Loading

        viewModelScope.launch {
            try {
                val result = planRepository.deleteActivity(dayIndex, activityIndex, planId, uid)

                result.onSuccess {
                    // Load lại kế hoạch sau khi xóa thành công
                    fetchPlanByIdFromFirebase(uid, planId)


                }.onFailure {
                    _planState.value =
                        PlanUiState.Error("Lỗi khi xóa hoạt động: ${it.localizedMessage}")
                }
            } catch (e: Exception) {
                _planState.value =
                    PlanUiState.Error("Có lỗi xảy ra khi xóa hoạt động: ${e.localizedMessage}")
            }
        }
    }

    //PlanViewModel
    fun deleteDayFromPlan(
        dayIndex: Int,
        planId: String,
        uid: String,
        navController: NavHostController
    ) {
        _planState.value = PlanUiState.Loading

        viewModelScope.launch {
            try {
                val result = planRepository.deleteDayFromPlan(dayIndex, planId, uid)

                result.onSuccess {
                    val plan = planRepository.getPlanById(uid, planId).getOrNull()

                    if (plan == null) {
                        navController.navigate("plan") {
                            popUpTo("home") { inclusive = true }
                        }
                        Log.d("PlanViewModel", "✅ Kế hoạch đã bị xóa hoàn toàn.")
                    } else {
                        fetchPlanByIdFromFirebase(uid, planId)
                        Log.d("PlanViewModel", "✅ Xóa ngày $dayIndex thành công")
                    }
                }.onFailure {
                    _planState.value =
                        PlanUiState.Error("❌ Lỗi khi xóa ngày: ${it.localizedMessage}")
                }
            } catch (e: Exception) {
                _planState.value =
                    PlanUiState.Error("❌ Lỗi xảy ra khi xóa ngày: ${e.localizedMessage}")
            }
        }
    }

    //PlanViewModel
    fun addDayToPlan(uid: String, planId: String, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            val result = planRepository.addDayToPlan(uid, planId)
            result.onSuccess { newDayIndex ->
                onSuccess(newDayIndex)
            }
            result.onFailure { exception ->

                Log.e("PlanViewModel", "Failed to add day: ${exception.localizedMessage}")
            }
        }
    }

    fun deletePlan(uid: String, planId: String) {
        viewModelScope.launch {
            _planState.value = PlanUiState.Loading
            try {
                val result = planRepository.deletePlan(uid, planId)

                result.onSuccess {
                    fetchPlansFromFirebase(uid)
                }.onFailure {
                    _planState.value =
                        PlanUiState.Error("Xóa kế hoạch thất bại: ${it.localizedMessage}")
                }

            } catch (e: Exception) {
                _planState.value = PlanUiState.Error("Xóa kế hoạch thất bại: ${e.localizedMessage}")
            }
        }
    }

//    fun askGemini(prompt: String, onResult: (String) -> Unit) {
//        _isLoading.value = true
//        viewModelScope.launch {
//            try {
//                // Gọi phương thức từ repository để lấy kết quả từ Gemini API
//                val result = planRepository.askGeminiFromRepo(prompt)
//                _isLoading.value = false
//                onResult(result) // Trả kết quả qua callback
//            } catch (e: Exception) {
//                _isLoading.value = false
//                onResult("Có lỗi xảy ra: ${e.localizedMessage}")
//            }
//        }
//    }


    fun createShareableLink(
        planId: String,
        uid: String,
        onLinkCreated: (String?) -> Unit
    ) {
        try {
            // 1. Deep link chứa thông tin uid và planId
            val deepLink = Uri.parse("https://myprojectdoancoso.com/plan?uid=$uid&planId=$planId")
            Log.d("PlanViewModel", "Creating dynamic link with deep link: $deepLink")

            // 2. Tạo dynamic link builder
            val dynamicLink = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setLink(deepLink)
                .setDomainUriPrefix("https://myprojectdoancoso.page.link")
                .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(1)
                        .build()
                )
                .buildDynamicLink()

            val fullDynamicLink = dynamicLink.uri.toString()

            // 3. Rút gọn link
            FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setLink(deepLink)
                .setDomainUriPrefix("https://myprojectdoancoso.page.link")
                .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(1)
                        .build()
                )
                .buildShortDynamicLink()
                .addOnSuccessListener { result ->
                    val shortLink = result.shortLink
                    Log.d("PlanViewModel", "Short link created: $shortLink")
                    onLinkCreated(shortLink?.toString())
                }
                .addOnFailureListener { e ->
                    Log.e("PlanViewModel", "Failed to shorten dynamic link", e)
                    onLinkCreated(fullDynamicLink) // Fallback: dùng link dài đã build trước đó
                }

        } catch (e: Exception) {
            Log.e("PlanViewModel", "Exception in createShareableLink", e)
            onLinkCreated(null)
        }
    }


    fun addOwnerToPlan(planId: String, currentUid: String, ownerUid: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _planState.value = PlanUiState.Loading

        viewModelScope.launch {
            try {
                // Lấy kế hoạch hiện tại
                val result = planRepository.getPlanById(currentUid, planId)
                if (result.isSuccess) {
                    val plan = result.getOrNull()
                    if (plan is PlanResultDb) {
                        val updatedOwners = plan.owners.toMutableList().apply {
                            if (!contains(ownerUid)) add(ownerUid)
                        }

                        val updatedPlan = plan.copy(owners = updatedOwners)

                        // Lưu lại kế hoạch với danh sách owner mới
                        val updateResult = planRepository.updatePlan(currentUid, updatedPlan, planId)

                        updateResult.onSuccess {
                            _planState.value = PlanUiState.Success(updatedPlan)
                            onSuccess()
                        }.onFailure {
                            _planState.value = PlanUiState.Error("Lỗi khi cập nhật owner: ${it.localizedMessage}")
                            onError("Lỗi khi cập nhật owner: ${it.localizedMessage}")
                        }
                    } else {
                        onError("Kế hoạch không đúng định dạng.")
                    }
                } else {
                    onError("Không tìm thấy kế hoạch.")
                }
            } catch (e: Exception) {
                _planState.value = PlanUiState.Error("Lỗi: ${e.localizedMessage}")
                onError("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    // Lấy userId chủ (từ path cha)
    fun getPlanOwnerId(planPath: String): String {
        // planPath: "plans/{userId}/{planId}"
        return planPath.split("/")[1]
    }

    // Lấy owner (từ trường owner trong plan)
    fun getPlanOwner(planId: String, userId: String, onResult: (String?) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("plans/$userId/$planId/owner")
        ref.get().addOnSuccessListener { snapshot ->
            onResult(snapshot.getValue(String::class.java))
        }.addOnFailureListener {
            onResult(null)
        }
    }

    /**
     * Kiểm tra user hiện tại có phải là chủ gốc hoặc chủ sở hữu của plan không
     * @param planId: id của plan
     * @param currentUserId: id user hiện tại
     * @param onResult: callback trả về true nếu là chủ gốc hoặc chủ sở hữu, false nếu không
     */
    fun checkIsRootOwnerOrOwner(
        rootOwnerId: String,
        planId: String,
        currentUserId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val planResult = planRepository.getPlanById(rootOwnerId, planId)
            if (planResult.isSuccess) {
                val plan = planResult.getOrNull()
                // currentUserId là rootOwner hoặc nằm trong danh sách owners
                if (plan is PlanResultDb && (rootOwnerId == currentUserId || plan.owners.contains(currentUserId))) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            } else {
                onResult(false)
            }
        }
    }


}


