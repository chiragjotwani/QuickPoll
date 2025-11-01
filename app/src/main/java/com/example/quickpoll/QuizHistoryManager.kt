package com.example.quickpoll

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class QuizHistoryManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("quiz_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveQuizResult(result: QuizResult) {
        val history = getQuizHistory().toMutableList()
        history.add(0, result) // Add at the beginning

        // Keep only last 50 attempts to avoid storage issues
        if (history.size > 50) {
            history.removeAt(history.size - 1)
        }

        val json = gson.toJson(history)
        sharedPreferences.edit().putString("history_list", json).apply()
    }

    fun getQuizHistory(): List<QuizResult> {
        val json = sharedPreferences.getString("history_list", null) ?: return emptyList()
        val type = object : TypeToken<List<QuizResult>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearHistory() {
        sharedPreferences.edit().clear().apply()
    }
}