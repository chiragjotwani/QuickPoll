package com.example.quickpoll

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizResult(
    val quizTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Float,
    val timestamp: Long,
    val timeTaken: String,
    val questions: List<QuestionResult>
) : Parcelable

@Parcelize
data class QuestionResult(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val userAnswer: String,
    val isCorrect: Boolean
) : Parcelable