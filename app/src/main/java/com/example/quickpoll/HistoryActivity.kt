package com.example.quickpoll

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickpoll.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyManager: QuizHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        historyManager = QuizHistoryManager(this)
        loadHistory()
    }

    private fun loadHistory() {
        val history = historyManager.getQuizHistory()

        if (history.isEmpty()) {
            binding.emptyStateText.visibility = View.VISIBLE
            binding.historyRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateText.visibility = View.GONE
            binding.historyRecyclerView.visibility = View.VISIBLE

            val adapter = HistoryAdapter(history) { quizResult ->
                // Open details activity
                val intent = Intent(this, QuizDetailActivity::class.java)
                intent.putExtra("quiz_result", quizResult)
                startActivity(intent)
            }

            binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.historyRecyclerView.adapter = adapter
        }
    }
}