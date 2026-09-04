package com.minesgame.data.local

import android.content.Context
import android.content.SharedPreferences
import com.minesgame.data.model.UserProfile
import com.minesgame.data.remote.dto.UserDto

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAuth(token: String, user: UserDto) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_ADDRESS, user.address ?: "")
            .putFloat(KEY_BALANCE, user.balance.toFloat())
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun updateProfile(username: String?, address: String?) {
        val editor = prefs.edit()
        if (username != null) editor.putString(KEY_USERNAME, username)
        if (address != null) editor.putString(KEY_ADDRESS, address)
        editor.apply()
    }

    fun updateBalance(balance: Double) {
        prefs.edit().putFloat(KEY_BALANCE, balance.toFloat()).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !getToken().isNullOrBlank()

    fun getBalance(): Double = prefs.getFloat(KEY_BALANCE, 1000.0f).toDouble()

    fun getUserProfile(): UserProfile {
        val loggedIn = isLoggedIn()
        return if (loggedIn) {
            UserProfile(
                username = prefs.getString(KEY_USERNAME, "Player") ?: "Player",
                email = prefs.getString(KEY_EMAIL, "") ?: "",
                address = prefs.getString(KEY_ADDRESS, "") ?: "",
                isGuest = false,
            )
        } else {
            UserProfile(
                username = "Guest",
                email = "",
                address = "",
                isGuest = true,
            )
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "mines_game_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_ADDRESS = "address"
        private const val KEY_BALANCE = "balance"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}
