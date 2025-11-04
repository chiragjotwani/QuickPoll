package com.example.quickpoll

data class QuizAttempt(
    val attemptId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val quizId: String = "",
    val quizTitle: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val percentage: Double = 0.0,
    val timestamp: Long = 0L,
    val timeTaken: Long = 0L, // Time in seconds
    val answers: Map<String, String> = emptyMap() // questionId to selectedAnswer
) {
    // Helper function to format timestamp
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    // Helper function to format time taken
    fun getFormattedTime(): String {
        val minutes = timeTaken / 60
        val seconds = timeTaken % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Helper function to get pass/fail status
    fun isPassed(passingPercentage: Double = 50.0): Boolean {
        return percentage >= passingPercentage
    }
}