package com.krebet.keuangandesakrebet.prefs

import android.content.Context

object Prefs {
    private const val PREF_NAME = "UserPrefs"
    private const val KEY = "isLogin"

    fun saveLoginStatus(context: Context, isLogin: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY, isLogin)
            .apply()
    }

    fun isLogin(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY, false)
}