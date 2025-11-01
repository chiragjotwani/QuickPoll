package com.example.quickpoll

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpoll.databinding.QuestionDetailItemBinding

class QuestionDetailAdapter(
    private val questions: List<QuestionResult>
) : RecyclerView.Adapter<QuestionDetailAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(private val binding: QuestionDetailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: QuestionResult, position: Int) {
            binding.apply {
                questionNumber.text = "Question ${position + 1}"
                questionText.text = question.question
                yourAnswer.text = if (question.userAnswer.isNotEmpty()) {
                    question.userAnswer
                } else {
                    "Not answered"
                }

                if (question.isCorrect) {
                    statusText.text = "✓ Correct"
                    statusText.setTextColor(Color.parseColor("#4CAF50"))
                    correctAnswerLabel.visibility = View.GONE
                    correctAnswer.visibility = View.GONE
                    yourAnswer.setBackgroundColor(Color.parseColor("#C8E6C9"))
                } else {
                    statusText.text = "✗ Incorrect"
                    statusText.setTextColor(Color.parseColor("#F44336"))
                    correctAnswerLabel.visibility = View.VISIBLE
                    correctAnswer.visibility = View.VISIBLE
                    correctAnswer.text = question.correctAnswer
                    yourAnswer.setBackgroundColor(Color.parseColor("#FFCDD2"))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = QuestionDetailItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    override fun getItemCount() = questions.size
}