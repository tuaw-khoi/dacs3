package com.example.doancoso.domain.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val userUidFlow: Flow<String?> = dataStore.data.map { it[USER_UID] }
    val userNameFlow: Flow<String?> = dataStore.data.map { it[USER_NAME] }
    val userEmailFlow: Flow<String?> = dataStore.data.map { it[USER_EMAIL] }

    suspend fun saveUser(uid: String, name: String, email: String) {
        dataStore.edit { prefs ->
            prefs[USER_UID] = uid
            prefs[USER_NAME] = name
            prefs[USER_EMAIL] = email
        }
    }

    suspend fun clearUser() {
        dataStore.edit { it.clear() }
    }
}
