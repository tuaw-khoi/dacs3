package com.example.doancoso.data.repository

import com.example.doancoso.data.models.DestinationDetails
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.data.remote.PlanService

class PlanRepository(private val api: PlanService,private val firebaseService: FirebaseService) {
    suspend fun getPlan(destination: String, startDate: String, endDate: String): PlanResult {
        val response = api.getPlans(destination, startDate, endDate)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Phản hồi rỗng từ server")
        } else {
            throw Exception("Lỗi API: ${response.code()} - ${response.message()}")
        }
    }

    suspend fun getDestinationDetails(destination: String): DestinationDetails {
        val response = api.getLocationDetails(destination)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Phản hồi rỗng từ server")
        } else {
            throw Exception("Lỗi không có thông tin địa điểm")
        }
    }

    fun savePlan(uid: String, plan: PlanResult, onComplete: (Boolean, String?) -> Unit) {
        firebaseService.savePlan(uid, plan, onComplete)
    }

    suspend fun getPlansFromDb(uid: String): Result<List<PlanResult>> {
        return firebaseService.getPlansFromDb(uid)
    }

    suspend fun getPlanById(uid: String, planId: String): Result<PlanResultDb> {
        return firebaseService.getPlanById(uid, planId)
    }
}
