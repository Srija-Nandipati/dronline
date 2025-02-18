package com.example.doctoronline.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.example.doctoronline.model.UserModel
import com.google.gson.Gson

object Utils {
    fun convertUserModelToJson(userModel: UserModel): String {
        val gson = Gson()
        return gson.toJson(userModel)
    }

    fun saveUserData(context: Context, userModel: UserModel) {
        val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val jsonString = convertUserModelToJson(userModel)
        editor.putString("user_model", jsonString)
        editor.apply()
    }



    fun getUserData(context: Context): UserModel?{
        val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString("user_model", null)

        if (jsonString != null) {
            val gson = Gson()
            return gson.fromJson(jsonString, UserModel::class.java)
        }

        return null
    }


    fun clearData(context: Context){
        val sp = context.getSharedPreferences("user", MODE_PRIVATE)
        val editor = sp.edit()
        editor.clear()
        editor.apply()
    }

}