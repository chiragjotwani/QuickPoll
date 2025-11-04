package com.example.quickpoll

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickpoll.databinding.ActivityCreateQuizBinding
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class CreateQuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateQuizBinding
    private val questionsList = mutableListOf<QuestionModel>()
    private lateinit var adapter: QuestionEditAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Create Quiz"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        adapter = QuestionEditAdapter(
            questionsList,
            onDeleteClick = { position ->
                showDeleteConfirmation(position)
            },
            onEditClick = { position ->
                editQuestion(position)
            }
        )
        binding.recyclerViewQuestions.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewQuestions.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnAddQuestion.setOnClickListener {
            showAddOrEditQuestionDialog(null)
        }

        binding.btnSaveQuiz.setOnClickListener {
            saveQuiz()
        }
    }

    private fun editQuestion(position: Int) {
        val question = questionsList[position]
        showAddOrEditQuestionDialog(question) { updatedQuestion ->
            questionsList[position] = updatedQuestion
            adapter.notifyItemChanged(position)
        }
    }

    private fun showAddOrEditQuestionDialog(
        existingQuestion: QuestionModel? = null,
        onUpdate: ((QuestionModel) -> Unit)? = null
    ) {
        val isEditMode = existingQuestion != null
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_question, null)

        val etQuestion = dialogView.findViewById<EditText>(R.id.etQuestion)
        val etOption1 = dialogView.findViewById<EditText>(R.id.etOption1)
        val etOption2 = dialogView.findViewById<EditText>(R.id.etOption2)
        val etOption3 = dialogView.findViewById<EditText>(R.id.etOption3)
        val etOption4 = dialogView.findViewById<EditText>(R.id.etOption4)
        val spinnerCorrect = dialogView.findViewById<Spinner>(R.id.spinnerCorrectAnswer)

        if (isEditMode) {
            etQuestion.setText(existingQuestion?.question)
            etOption1.setText(existingQuestion?.options?.getOrNull(0) ?: "")
            etOption2.setText(existingQuestion?.options?.getOrNull(1) ?: "")
            etOption3.setText(existingQuestion?.options?.getOrNull(2) ?: "")
            etOption4.setText(existingQuestion?.options?.getOrNull(3) ?: "")

            val optionsArray = existingQuestion?.options?.toTypedArray() ?: emptyArray()
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, optionsArray)
            spinnerCorrect.adapter = spinnerAdapter
            val correctIndex = existingQuestion?.options?.indexOf(existingQuestion.correct) ?: 0
            spinnerCorrect.setSelection(correctIndex)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (isEditMode) "Edit Question" else "Add Question")
            .setView(dialogView)
            .setPositiveButton(if (isEditMode) "Update" else "Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val questionText = etQuestion.text.toString().trim()
                val option1 = etOption1.text.toString().trim()
                val option2 = etOption2.text.toString().trim()
                val option3 = etOption3.text.toString().trim()
                val option4 = etOption4.text.toString().trim()
                val options = listOf(option1, option2, option3, option4)

                if (validateQuestion(questionText, options)) {
                    val selectedCorrectOption = spinnerCorrect.selectedItem.toString()
                    val newOrUpdatedQuestion = QuestionModel(
                        id = existingQuestion?.id ?: UUID.randomUUID().toString(),
                        question = questionText,
                        options = options,
                        correct = selectedCorrectOption
                    )

                    if (isEditMode) {
                        onUpdate?.invoke(newOrUpdatedQuestion)
                    } else {
                        questionsList.add(newOrUpdatedQuestion)
                        adapter.notifyItemInserted(questionsList.size - 1)
                        updateQuestionCount()
                    }
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }


    private fun validateQuestion(question: String, options: List<String>): Boolean {
        if (question.isEmpty()) {
            Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show()
            return false
        }
        if (options.any { it.isEmpty() }) {
            Toast.makeText(this, "All options must be filled", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showDeleteConfirmation(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Question")
            .setMessage("Are you sure you want to delete this question?")
            .setPositiveButton("Delete") { _, _ ->
                questionsList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, questionsList.size)
                updateQuestionCount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateQuestionCount() {
        binding.tvQuestionCount.text = "Questions: ${questionsList.size}"
    }

    private fun saveQuiz() {
        val title = binding.etQuizTitle.text.toString().trim()
        val subtitle = binding.etQuizSubtitle.text.toString().trim()
        val time = binding.etQuizTime.text.toString().trim()

        if (title.isEmpty() || subtitle.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill all quiz details", Toast.LENGTH_SHORT).show()
            return
        }

        if (questionsList.isEmpty()) {
            Toast.makeText(this, "Please add at least one question", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveQuiz.isEnabled = false

        val quizId = UUID.randomUUID().toString()
        val quizRef = FirebaseDatabase.getInstance().reference.child("Quizzes").child(quizId)

        val quizMetadata = QuizModel(
            id = quizId,
            title = title,
            subtitle = subtitle,
            time = time
        )

        quizRef.setValue(quizMetadata).addOnSuccessListener {
            // Now save the questions in a sub-collection
            val questionsMap = questionsList.associateBy { it.id }
            quizRef.child("questions").setValue(questionsMap)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Quiz created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveQuiz.isEnabled = true
                    Toast.makeText(this, "Failed to save questions: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }.addOnFailureListener { e ->
            binding.progressBar.visibility = View.GONE
            binding.btnSaveQuiz.isEnabled = true
            Toast.makeText(this, "Failed to create quiz: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
