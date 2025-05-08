package com.example.doancoso.data.repository

import android.content.Context
import android.util.Log
import com.example.doancoso.data.models.DayPlanDb
import com.example.doancoso.data.models.DestinationDetails
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.data.remote.PlanService

class PlanRepository(private val api: PlanService, private val firebaseService: FirebaseService) {
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

    suspend fun updatePlan(uid: String, updatedPlan: PlanResultDb, planId: String): Result<Unit> {
        return firebaseService.updatePlan(uid, updatedPlan, planId)
    }

    suspend fun updateDayPlan(
        uid: String,
        planId: String,
        dayIndex: Int,
        updatedDayPlan: DayPlanDb
    ): Result<Unit> {
        return try {
            firebaseService.updateDayPlan(uid, planId, dayIndex, updatedDayPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteActivity(
        dayIndex: Int,
        activityIndex: Int,
        planId: String,
        uid: String
    ): Result<Unit> {
        return firebaseService.deleteActivityFromPlan(uid, planId, dayIndex, activityIndex)
    }

    suspend fun deleteDayFromPlan(
        dayIndex: Int,
        planId: String,
        uid: String
    ): Result<Unit> {
        return try {
            firebaseService.deleteDayFromPlan(uid, planId, dayIndex)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addDayToPlan(uid: String, planId: String): Result<Int> {
        return firebaseService.addDayToPlan(uid, planId)
    }

    suspend fun deletePlan(uid: String, planId: String): Result<Unit> {
        return try {
            firebaseService.deletePlan(uid, planId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



}
