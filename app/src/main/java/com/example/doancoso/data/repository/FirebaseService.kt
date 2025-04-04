package com.example.doancoso.data.repository

import android.util.Log
import com.example.doancoso.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

class FirebaseService {
     val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    fun registerUser(email: String, password: String, name: String, onComplete: (Boolean, String?) -> Unit) {
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
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid.isNullOrEmpty()) {
                        onComplete(false, "User UID is empty.", null)
                        return@addOnCompleteListener
                    }
                    fetchUser(uid) { user ->
                        if (user != null) {
                            onComplete(true, null, user)
                        } else {
                            onComplete(false, "Failed to fetch user data from database.", null)
                        }
                    }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Login failed."
                    Log.e("FirebaseService", "Login error: $errorMessage")
                    onComplete(false, errorMessage, null)
                }
            }
    }

    private fun fetchUser(uid: String, onComplete: (User?) -> Unit) {
        database.child(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue<User>()
                onComplete(user)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun createPlan() {

    }


}
