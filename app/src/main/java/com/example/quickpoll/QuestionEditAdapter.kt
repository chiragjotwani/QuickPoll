package com.example.quickpoll

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpoll.databinding.ItemQuestionEditBinding

class QuestionEditAdapter(
    private val questions: List<QuestionModel>,
    private val onDeleteClick: (Int) -> Unit,
    private val onEditClick: (Int) -> Unit
) : RecyclerView.Adapter<QuestionEditAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(private val binding: ItemQuestionEditBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: QuestionModel, position: Int) {
            binding.apply {
                tvQuestionNumber.text = "Question ${position + 1}"
                tvQuestion.text = question.question

                // Display options
                val optionsText = question.options.mapIndexed { index, option ->
                    "${('A' + index)}. $option"
                }.joinToString("\n")
                tvOptions.text = optionsText

                // Highlight correct answer
                val correctIndex = question.options.indexOf(question.correct)
                tvCorrectAnswer.text = "Correct: ${('A' + correctIndex)} - ${question.correct}"

                btnEdit.setOnClickListener {
                    onEditClick(position)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionEditBinding.inflate(
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