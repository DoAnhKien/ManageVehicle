package com.example.vehiclessale

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

inline fun <reified T> genericType(): Type = object : TypeToken<T>() {}.type

class UserPrefs {
    companion object {
        const val APP = " APP"
        val USER: String = "USER_LOCAL_DATA"

        val USER_TEMP: String = "USER_TEMP"
        val NUMBER_CART: String = "NUMBER CART"
        val NUMBER_NOTI: String = "NUMBER NOTI"

        private fun getSharedPreferences(context: Context): SharedPreferences? {
            return context.getSharedPreferences(APP, Context.MODE_PRIVATE)
        }

        fun getLocalData(context: Context): String? {
            return getSharedPreferences(context)?.getString(USER, null)
        }

        fun setLocalData(context: Context, newValue: String) {
            getSharedPreferences(context)?.edit()?.also {
                it.putString(USER, newValue)
                it.apply()
            }
        }

        fun getNumberCartData(context: Context): String? {
            return getSharedPreferences(context)?.getString(NUMBER_CART, null)
        }


        fun setNumberNotify(context: Context, newValue: String) {
            getSharedPreferences(context)?.edit()?.also {
                it.putString(NUMBER_NOTI, newValue)
                it.apply()
            }
        }

        fun getNumberNotify(context: Context): String? {
            return getSharedPreferences(context)?.getString(NUMBER_NOTI, null)
        }


        fun setNumberCartData(context: Context, newValue: String) {
            getSharedPreferences(context)?.edit()?.also {
                it.putString(NUMBER_CART, newValue)
                it.apply()
            }
        }

        fun setUserTemp(context: Context, newValue: String) {
            getSharedPreferences(context)?.edit()?.also {
                it.putString(USER_TEMP, newValue)
                it.apply()
            }
        }

        fun getUserTemp(context: Context): String? {
            return getSharedPreferences(context)?.getString(USER_TEMP, null)
        }

        fun clearPrefs(context: Context, key: String) {
            getSharedPreferences(context)?.edit()?.also {
                it.remove(key).apply()
            }
        }

        fun clearData(context: Context) {
            getSharedPreferences(context)?.edit()?.also {
                it.clear().apply()
            }
        }
    }
}