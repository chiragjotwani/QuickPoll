package com.example.quickpoll

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickpoll.databinding.ActivityQuizDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class QuizDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        val quizResult = intent.getParcelableExtra<QuizResult>("quiz_result")
        quizResult?.let { displayQuizDetails(it) }
    }

    private fun displayQuizDetails(result: QuizResult) {
        binding.apply {
            quizTitle.text = result.quizTitle
            percentageText.text = "${result.percentage.toInt()}%"
            scoreText.text = "Score: ${result.score}/${result.totalQuestions}"
            timeTakenText.text = "Time: ${result.timeTaken}"
            dateText.text = formatDate(result.timestamp)

            // Change color based on pass/fail
            val color = if (result.percentage >= 60) {
                android.graphics.Color.parseColor("#4CAF50")
            } else {
                android.graphics.Color.parseColor("#F44336")
            }
            percentageText.setTextColor(color)

            // Setup questions recycler view
            val adapter = QuestionDetailAdapter(result.questions)
            questionsRecyclerView.layoutManager = LinearLayoutManager(this@QuizDetailActivity)
            questionsRecyclerView.adapter = adapter
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}