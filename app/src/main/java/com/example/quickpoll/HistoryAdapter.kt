package com.example.quickpoll

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpoll.databinding.HistoryItemBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val historyList: List<QuizResult>,
    private val onItemClick: (QuizResult) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: QuizResult) {
            binding.apply {
                quizTitle.text = result.quizTitle
                percentageText.text = "${result.percentage.toInt()}%"
                scoreText.text = "Score: ${result.score}/${result.totalQuestions}"
                timeTakenText.text = "Time: ${result.timeTaken}"
                dateText.text = formatDate(result.timestamp)
                progressBar.progress = result.percentage.toInt()

                // Change color based on pass/fail
                val color = if (result.percentage >= 60) {
                    android.graphics.Color.parseColor("#4CAF50") // Green
                } else {
                    android.graphics.Color.parseColor("#F44336") // Red
                }
                percentageText.setTextColor(color)

                viewDetailsBtn.setOnClickListener {
                    onItemClick(result)
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount() = historyList.size
}