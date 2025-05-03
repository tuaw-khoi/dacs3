package com.example.doancoso.data.repository

import android.util.Log
import com.example.doancoso.data.models.DayPlanDb
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await

class FirebaseService {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    fun registerUser(
        email: String,
        password: String,
        name: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid

                    if (uid != null) {
                        val user = User(uid, name, email)
                        Log.d("test", "${user}")
                        database.child(uid).setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    onComplete(true, null)
                                } else {
                                    onComplete(false, dbTask.exception?.message)
                                }
                            }
                    } else {
                        onComplete(false, "Failed to retrieve user UID")
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, onComplete: (Boolean, String?, User?) -> Unit) {
        Log.e("user", "user : $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("user", "user : $email")
                    val uid = auth.currentUser?.uid
                    if (uid.isNullOrEmpty()) {
                        onComplete(false, "User UID is empty.", null)
                        return@addOnCompleteListener
                    }
                    fetchUser(uid) { user ->
                        if (user != null) {
                            Log.e("FirebaseService", "user : $user")
                            onComplete(true, null, user)
                        } else {
                            Log.e("FirebaseService", "user1 : $user")
                            onComplete(false, "Failed to fetch user data from database.", null)
                        }
                    }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Login failed."
                    Log.e("FirebaseService", "Login error: $errorMessage")
                    onComplete(false, errorMessage, null)
                }
                if (task.isCanceled) {
                    Log.e("FirebaseService", "Login task was canceled.")
                }
            }
    }

    private fun fetchUser(uid: String, onComplete: (User?) -> Unit) {
        database.child(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue<User>()
                Log.d("FirebaseService", "Dữ liệu user từ database: $user")
                onComplete(user)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun savePlan(uid: String, plan: PlanResult, onComplete: (Boolean, String?) -> Unit) {
        val plansRef = FirebaseDatabase.getInstance().getReference("plans").child(uid)
        val planId = plansRef.push().key

        if (planId == null) {
            onComplete(false, "Không thể tạo ID cho kế hoạch.")
            return
        }

        plansRef.child(planId).setValue(plan)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Lưu kế hoạch thành công: $planId")
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseService", "Lỗi khi lưu kế hoạch: ${exception.message}")
                onComplete(false, exception.message)
            }
    }

    // Cập nhật FirebaseService để trả về Result trực tiếp
    suspend fun getPlansFromDb(uid: String): Result<List<PlanResult>> {
        return try {
            val plansRef = FirebaseDatabase.getInstance().getReference("plans").child(uid)
            val snapshot = plansRef.get().await()

            if (snapshot.exists()) {
                val plansList = mutableListOf<PlanResult>()
                snapshot.children.forEach { planSnapshot ->
                    val plan = planSnapshot.getValue(PlanResult::class.java)
                    plan?.let {
                        it.uid = planSnapshot.key
                        plansList.add(it)
                    }
                }
                Result.success(plansList)  // Trả về Result chứa danh sách kế hoạch
            } else {
                Result.failure(Exception("No plans found for this user"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching plans from database", e)  // Log lỗi chi tiết
            Result.failure(e)  // Trả về Result chứa lỗi nếu có ngoại lệ
        }
    }


    suspend fun getPlanById(uid: String, planId: String): Result<PlanResultDb> {
        return try {
            val planRef = FirebaseDatabase.getInstance()
                .getReference("plans")
                .child(uid)
                .child(planId)

            val snapshot = planRef.get().await()

            if (snapshot.exists()) {
                val plan = snapshot.getValue(PlanResultDb::class.java)
                if (plan != null) {
                    plan.uid = uid  // Nếu cần gán lại uid
                    Result.success(plan)
                } else {
                    Result.failure(Exception("Plan not found"))
                }
            } else {
                Result.failure(Exception("Plan not found"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching plan by id", e)
            Result.failure(e)
        }
    }

    // Cập nhật kế hoạch từ Firebase
    suspend fun updatePlan(uid: String, updatedPlan: PlanResultDb, planId: String): Result<Unit> {
        return try {

            val ref = FirebaseDatabase.getInstance()
                .getReference("plans")
                .child(uid)
                .child(planId)

            ref.setValue(updatedPlan).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // FirebaseService
    suspend fun updateDayPlan(
        uid: String,
        planId: String,
        dayIndex: Int,
        updatedDayPlan: DayPlanDb
    ): Result<Unit> {
        return try {
            val planRef = FirebaseDatabase.getInstance().getReference("plans")
                .child(uid)
                .child(planId)
                .child("itinerary")
                .child("itinerary")
                .child(dayIndex.toString())

            planRef.setValue(updatedDayPlan).await()

            Log.d("FirebaseService", "✅ Cập nhật thành công ngày $dayIndex: $updatedDayPlan")
            Log.d("FirebaseService", "✅ Path: plans/$uid/$planId/itinerary/itinerary/$dayIndex")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "❌ Lỗi khi cập nhật ngày $dayIndex: ${e.localizedMessage}")
            Result.failure(e)
        }



    }


    // FirebaseService
    suspend fun deleteActivityFromPlan(
        uid: String,
        planId: String,
        dayIndex: Int,
        activityIndex: Int
    ): Result<Unit> {
        return try {
            // Lấy kế hoạch từ Firebase
            val planRef = FirebaseDatabase.getInstance()
                .getReference("plans")
                .child(uid)
                .child(planId)
                .child("itinerary")
                .child("itinerary")
                .child(dayIndex.toString())

            // Lấy thông tin ngày trong kế hoạch (KTX API)
            val snapshot = planRef.get().await()

            if (snapshot.exists()) {
                // Lấy dữ liệu ngày kế hoạch bằng KTX API
                val dayPlan = snapshot.getValue(DayPlanDb::class.java)

                // Kiểm tra nếu dayPlan không null và có activities
                dayPlan?.activities?.let { activities ->
                    // Xóa hoạt động tại chỉ số activityIndex
                    if (activityIndex in activities.indices) {
                        val updatedActivities = activities.toMutableList()
                        updatedActivities.removeAt(activityIndex)

                        // Cập nhật lại ngày kế hoạch với danh sách hoạt động đã xóa
                        dayPlan.activities = updatedActivities

                        // Lưu lại kế hoạch đã cập nhật
                        planRef.setValue(dayPlan).await()

                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Invalid activity index"))
                    }
                } ?: run {
                    Result.failure(Exception("Day plan has no activities"))
                }
            } else {
                Result.failure(Exception("Day plan not found"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "❌ Lỗi khi xóa hoạt động: ${e.localizedMessage}")
            Result.failure(e)
        }
    }



}
