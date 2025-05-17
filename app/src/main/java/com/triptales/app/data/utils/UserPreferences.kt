package com.triptales.app.data.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A utility class to manage user preferences using DataStore.
 * Provides type-safe access to preferences with strong typing.
 */
class UserPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "trip_tales_preferences")

        // Authentication related keys
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY = intPreferencesKey("user_id")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_PROFILE_IMAGE_KEY = stringPreferencesKey("user_profile_image")

        // App settings keys
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val DATA_SAVER_KEY = booleanPreferencesKey("data_saver")
        val LAST_SYNC_TIME_KEY = longPreferencesKey("last_sync_time")
        val LANGUAGE_KEY = stringPreferencesKey("language")

        // Cache control keys
        val CACHE_EXPIRY_KEY = longPreferencesKey("cache_expiry")

        // Current group
        val CURRENT_GROUP_ID_KEY = intPreferencesKey("current_group_id")
    }

    // String preferences
    suspend fun saveString(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getString(key: androidx.datastore.preferences.core.Preferences.Key<String>): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[key]
        }
    }

    // Int preferences
    suspend fun saveInt(key: androidx.datastore.preferences.core.Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getInt(key: androidx.datastore.preferences.core.Preferences.Key<Int>): Flow<Int?> {
        return context.dataStore.data.map { preferences ->
            preferences[key]
        }
    }

    // Boolean preferences
    suspend fun saveBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, defaultValue: Boolean = false): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    // Long preferences
    suspend fun saveLong(key: androidx.datastore.preferences.core.Preferences.Key<Long>, value: Long) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getLong(key: androidx.datastore.preferences.core.Preferences.Key<Long>): Flow<Long?> {
        return context.dataStore.data.map { preferences ->
            preferences[key]
        }
    }

    // Clear specific preference
    suspend fun clearPreference(key: androidx.datastore.preferences.core.Preferences.Key<*>) {
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    // Clear all preferences
    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Authentication helpers
    suspend fun saveUserSession(accessToken: String, refreshToken: String, userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_PROFILE_IMAGE_KEY)
        }
    }

    suspend fun saveUserProfile(name: String, email: String, profileImage: String?) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
            if (profileImage != null) {
                preferences[USER_PROFILE_IMAGE_KEY] = profileImage
            }
        }
    }

    // App settings helpers
    suspend fun setDarkMode(enabled: Boolean) {
        saveBoolean(DARK_MODE_KEY, enabled)
    }

    fun isDarkModeEnabled(): Flow<Boolean> {
        return getBoolean(DARK_MODE_KEY)
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        saveBoolean(NOTIFICATION_ENABLED_KEY, enabled)
    }

    fun areNotificationsEnabled(): Flow<Boolean> {
        return getBoolean(NOTIFICATION_ENABLED_KEY, true)
    }

    suspend fun setCurrentGroupId(groupId: Int) {
        saveInt(CURRENT_GROUP_ID_KEY, groupId)
    }

    fun getCurrentGroupId(): Flow<Int?> {
        return getInt(CURRENT_GROUP_ID_KEY)
    }
}