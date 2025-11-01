package com.example.quickpoll

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quickpoll.databinding.ActivityQuizBinding
import com.example.quickpoll.databinding.ScoreDialogBinding

class QuizActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        var questionModelList: List<QuestionModel> = listOf()
        var time: String = ""
        var quizTitle: String = "" // Add this
    }

    lateinit var binding: ActivityQuizBinding
    var currentQuestionIndex = 0
    var selectedAnswer = ""
    var score = 0

    // Add these to track user answers
    private val userAnswers = mutableListOf<String>()
    private var startTime: Long = 0
    private var endTime: Long = 0

    private fun loadQuestions() {
        selectedAnswer = ""

        if (currentQuestionIndex == questionModelList.size) {
            finishQuiz()
            return
        }

        binding.apply {
            questionIndicatorTextview.text = "Question ${currentQuestionIndex + 1}/${questionModelList.size}"
            questionProgressIndicator.progress =
                ((currentQuestionIndex + 1).toFloat() / questionModelList.size * 100).toInt()
            questionTextview.text = questionModelList[currentQuestionIndex].question
            btn0.text = questionModelList[currentQuestionIndex].options[0]
            btn1.text = questionModelList[currentQuestionIndex].options[1]
            btn2.text = questionModelList[currentQuestionIndex].options[2]
            btn3.text = questionModelList[currentQuestionIndex].options[3]
        }
    }

    private fun startTimer() {
        val totalTimeInMillis = time.toLong() * 60 * 1000L

        object : android.os.CountDownTimer(totalTimeInMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                binding.timerIndicatorTextview.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                // Time's up - finish the quiz
                Toast.makeText(this@QuizActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                finishQuiz()
            }
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startTime = System.currentTimeMillis() // Track start time

        binding.apply {
            btn0.setOnClickListener(this@QuizActivity)
            btn1.setOnClickListener(this@QuizActivity)
            btn2.setOnClickListener(this@QuizActivity)
            btn3.setOnClickListener(this@QuizActivity)
            nextBtn.setOnClickListener(this@QuizActivity)
        }

        // Get quiz title from intent
        quizTitle = intent.getStringExtra("quiz_title") ?: "Quiz"

        if(intent.hasExtra("question_list")){
            questionModelList = intent.getParcelableArrayListExtra("question_list")?: emptyList()
        }

        if(questionModelList.isNotEmpty()){
            loadQuestions()
            startTimer()
        } else{
            Toast.makeText(this, "Failed to load questions", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onClick(view: View){
        binding.apply {
            btn0.setBackgroundColor(getColor(R.color.grey))
            btn1.setBackgroundColor(getColor(R.color.grey))
            btn2.setBackgroundColor(getColor(R.color.grey))
            btn3.setBackgroundColor(getColor(R.color.grey))
        }

        val clickedBtn = view as Button
        if(clickedBtn.id == R.id.next_btn){
            // Save user answer (empty if not selected)
            userAnswers.add(selectedAnswer)

            if(selectedAnswer == questionModelList[currentQuestionIndex].correct){
                score++
                Log.i("Score of Quiz", score.toString())
            }
            currentQuestionIndex++
            loadQuestions()
        } else {
            clickedBtn.setBackgroundColor(getColor(R.color.orange))
            selectedAnswer = clickedBtn.text.toString()
        }
    }

    private fun finishQuiz(){
        endTime = System.currentTimeMillis()
        val timeTaken = formatTimeTaken(endTime - startTime)

        val totalQuestions = questionModelList.size
        val percentage = (score.toFloat() / totalQuestions) * 100

        // Save to history
        saveQuizToHistory(percentage, timeTaken)

        val dialogBinding = ScoreDialogBinding.inflate(layoutInflater)
        dialogBinding.apply{
            scoreProgressIndicator.progress = percentage.toInt()
            scoreProgressText.text = "$percentage%"
            if(percentage > 60){
                scoreTitle.text = "Congrats! You have passed"
                scoreTitle.setTextColor(Color.GREEN)
            } else {
                scoreTitle.text = "Sorry! You have failed"
                scoreTitle.setTextColor(Color.RED)
            }
            scoreSubtitle.text = "$score out of $totalQuestions questions are correct"
            finishBtn.setOnClickListener {
                finish()
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
    }

    private fun saveQuizToHistory(percentage: Float, timeTaken: String) {
        val questionResults = questionModelList.mapIndexed { index, question ->
            val userAnswer = if (index < userAnswers.size) userAnswers[index] else ""
            QuestionResult(
                question = question.question,
                options = question.options,
                correctAnswer = question.correct,
                userAnswer = userAnswer,
                isCorrect = userAnswer == question.correct
            )
        }

        val quizResult = QuizResult(
            quizTitle = quizTitle,
            score = score,
            totalQuestions = questionModelList.size,
            percentage = percentage,
            timestamp = System.currentTimeMillis(),
            timeTaken = timeTaken,
            questions = questionResults
        )

        QuizHistoryManager(this).saveQuizResult(quizResult)
    }

    private fun formatTimeTaken(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}