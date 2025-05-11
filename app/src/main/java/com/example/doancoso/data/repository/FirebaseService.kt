package com.example.doancoso.data.repository

import android.net.Uri
import android.util.Log
import com.example.doancoso.data.models.DayPlanDb
import com.example.doancoso.data.models.ItineraryDb
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
//                    plan.uid = uid  // Nếu cần gán lại uid
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

            Log.d("FirebaseService", "✅ Updated day $dayIndex successfully: $updatedDayPlan")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "❌ Error updating day $dayIndex: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun deleteActivityFromPlan(
        uid: String,
        planId: String,
        dayIndex: Int,
        activityIndex: Int
    ): Result<Unit> {
        return try {
            val planRef = FirebaseDatabase.getInstance()
                .getReference("plans")
                .child(uid)
                .child(planId)
                .child("itinerary")
                .child("itinerary")
                .child(dayIndex.toString())

            val snapshot = planRef.get().await()

            if (snapshot.exists()) {
                val dayPlan = snapshot.getValue(DayPlanDb::class.java)

                if (dayPlan != null && dayPlan.activities != null) {
                    val activities = dayPlan.activities
                    if (activityIndex in activities.indices) {
                        // Xóa hoạt động tại activityIndex
                        val updatedActivities = activities.toMutableList()
                        updatedActivities.removeAt(activityIndex)
                        dayPlan.activities = updatedActivities

                        if (dayPlan.activities.isEmpty()) {
                            // Nếu không còn hoạt động nào, xóa toàn bộ ngày
                            val itineraryRef = FirebaseDatabase.getInstance()
                                .getReference("plans")
                                .child(uid)
                                .child(planId)
                                .child("itinerary")
                                .child("itinerary")
                            itineraryRef.child(dayIndex.toString()).removeValue().await()

                            // Cập nhật lại ngày bắt đầu và kết thúc nếu không còn ngày nào
                            updatePlanDatesAndDays(uid, planId)
                        } else {
                            // Nếu vẫn còn hoạt động, cập nhật lại ngày
                            planRef.setValue(dayPlan).await()
                        }

                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Invalid activity index"))
                    }
                } else {
                    Result.failure(Exception("Day plan has no activities"))
                }
            } else {
                Result.failure(Exception("Day plan not found"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "❌ Error deleting activity: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    private suspend fun updatePlanDatesAndDays(uid: String, planId: String) {
        val planRef = FirebaseDatabase.getInstance()
            .getReference("plans")
            .child(uid)
            .child(planId)
            .child("itinerary")

        val snapshot = planRef.get().await()

        if (snapshot.exists()) {
            val itinerary = snapshot.getValue(ItineraryDb::class.java)

            itinerary?.let { planItinerary ->
                val updatedItinerary =
                    planItinerary.itinerary.filterNot { it.activities.isEmpty() }

                if (updatedItinerary.isNotEmpty()) {
                    val startDate = planItinerary.startDate
                    var endDate = planItinerary.endDate

                    if (endDate.isNotEmpty()) {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val localDate = LocalDate.parse(endDate, formatter)

                        val newEndDate = localDate.minusDays(1)

                        // Chuyển lại thành String và gán lại cho endDate
                        endDate = newEndDate.format(formatter)
                    }
                    planItinerary.startDate = startDate
                    planItinerary.endDate = endDate

                    Log.d("FirebaseService", "ngày bắt đầu: $startDate, ngày kết thúc: $endDate")

                    // Update the number of days
                    planItinerary.itinerary = updatedItinerary
                } else {
                    planItinerary.startDate = ""  // No activities left, clear start date
                    planItinerary.endDate = ""    // No activities left, clear end date
                }

                // Set the updated itinerary back to Firebase
                planRef.setValue(planItinerary).await()
            }
        }
    }

    suspend fun deleteDayFromPlan(uid: String, planId: String, dayIndex: Int): Result<Unit> {
        return try {
            val itineraryRef = FirebaseDatabase.getInstance()
                .getReference("plans")
                .child(uid)
                .child(planId)
                .child("itinerary")

            val snapshot = itineraryRef.get().await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("Itinerary not found"))
            }

            val itinerary = snapshot.getValue(ItineraryDb::class.java)

            if (itinerary == null || itinerary.itinerary.size <= dayIndex) {
                return Result.failure(Exception("Invalid day index"))
            }

            // Cập nhật danh sách các ngày
            val updatedDays = itinerary.itinerary.toMutableList()
            updatedDays.removeAt(dayIndex)


            // Nếu không còn ngày nào, xóa ngày bắt đầu và kết thúc
            if (updatedDays.isEmpty()) {
                val planRef = FirebaseDatabase.getInstance()
                    .getReference("plans")
                    .child(uid)
                    .child(planId)

                planRef.removeValue().await()
                Log.d("FirebaseService", "✅ Không còn ngày nào - Đã xóa toàn bộ kế hoạch $planId")
                return Result.success(Unit)
            } else {
                // Cập nhật lại ngày kết thúc
                val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                val startDate = LocalDate.parse(itinerary.startDate, formatter)

                val newEndDate = startDate.plusDays(updatedDays.size.toLong() - 1)
                itinerary.endDate = newEndDate.format(formatter)
            }

            itinerary.itinerary = updatedDays

            // Cập nhật lại lịch trình trong Firebase
            itineraryRef.setValue(itinerary).await()

            Log.d("FirebaseService", "✅ Đã xóa ngày $dayIndex khỏi kế hoạch $planId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "❌ Lỗi khi xóa ngày khỏi kế hoạch: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun addDayToPlan(uid: String, planId: String): Result<Int> {
        return try {
            val itineraryRef = FirebaseDatabase.getInstance()
                .getReference("plans")
                .child(uid)
                .child(planId)
                .child("itinerary")

            val snapshot = itineraryRef.get().await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("Itinerary not found"))
            }

            val itinerary = snapshot.getValue(ItineraryDb::class.java)
                ?: return Result.failure(Exception("Failed to parse itinerary"))

            val newDayIndex = itinerary.itinerary.size

            // Tạo một ngày mới rỗng
            val newDay = DayPlanDb(
                activities = mutableListOf()
            )

            val updatedList = itinerary.itinerary.toMutableList()
            updatedList.add(newDay)

            // Cập nhật ngày kết thúc mới nếu có startDate
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            var endDate = itinerary.endDate

            if (itinerary.startDate.isNotEmpty()) {
                val startDate = LocalDate.parse(itinerary.startDate, formatter)
                val newEndDate = startDate.plusDays(updatedList.size.toLong() - 1)
                endDate = newEndDate.format(formatter)
            }

            val updatedItinerary = itinerary.copy(
                itinerary = updatedList,
                endDate = endDate
            )

            itineraryRef.setValue(updatedItinerary).await()

            Log.d("FirebaseService", "Updating itinerary: $updatedItinerary")

            Result.success(newDayIndex) // Trả về chỉ số của ngày mới để điều hướng
        } catch (e: Exception) {
            Log.e("FirebaseService", "❌ Error adding new day: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun deletePlan(uid: String, planId: String): Result<Unit> {
        return try {
            val planRef = FirebaseDatabase.getInstance()
                .getReference("plans")
                .child(uid)
                .child(planId)

            planRef.removeValue().await()
            Log.d("FirebaseService", "✅ Đã xóa kế hoạch $planId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "❌ Lỗi khi xóa kế hoạch: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    //firebaseService
    fun updateUserProfile(
        uid: String,
        updatedUser: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userRef = database.child(uid)

        userRef.setValue(updatedUser)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null) // Success
                } else {
                    onComplete(false, task.exception?.message) // Failure
                }
            }
    }

    fun getUserIdOfPlan(planId: String, onResult: (String?) -> Unit) {
    val plansRef = FirebaseDatabase.getInstance().getReference("plans")
    plansRef.get().addOnSuccessListener { snapshot ->
        for (userSnapshot in snapshot.children) {
            val userId = userSnapshot.key
            if (userSnapshot.hasChild(planId)) {
                onResult(userId)
                return@addOnSuccessListener
            }
        }
        onResult(null) // Không tìm thấy
    }.addOnFailureListener {
        onResult(null)
    }
}


}
