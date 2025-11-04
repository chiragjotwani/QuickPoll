package com.example.quickpoll

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpoll.databinding.ItemResultBinding

class AllResultsAdapter(
    private val attempts: List<QuizAttempt>
) : RecyclerView.Adapter<AllResultsAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(private val binding: ItemResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(attempt: QuizAttempt) {
            binding.apply {
                tvUserName.text = attempt.userName
                tvQuizTitle.text = attempt.quizTitle
                tvScore.text = "${attempt.score}/${attempt.totalQuestions}"
                tvPercentage.text = String.format("%.1f%%", attempt.percentage)
                tvDate.text = attempt.getFormattedDate()
                tvTimeTaken.text = "Time: ${attempt.getFormattedTime()}"

                // Color code based on performance
                val color = when {
                    attempt.percentage >= 80 -> R.color.success_green
                    attempt.percentage >= 50 -> R.color.warning_orange
                    else -> R.color.error_red
                }
                tvPercentage.setTextColor(
                    ContextCompat.getColor(binding.root.context, color)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(attempts[position])
    }

    override fun getItemCount() = attempts.size
}