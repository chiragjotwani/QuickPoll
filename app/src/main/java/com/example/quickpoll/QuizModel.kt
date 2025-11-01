package com.example.quickpoll
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizModel(
    var id: String = "",
    var title: String = "",
    var subtitle: String = "",
    var time: String = "",
    var questionList: List<QuestionModel> = emptyList()
): Parcelable

@Parcelize
data class QuestionModel(
    var question: String = "",
    var options: List<String> = emptyList(),
    var correct: String = ""
): Parcelable