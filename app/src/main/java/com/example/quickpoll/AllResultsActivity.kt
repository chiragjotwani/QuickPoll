package com.example.quickpoll

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickpoll.databinding.ActivityAllResultsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllResultsBinding
    private val attemptsList = mutableListOf<QuizAttempt>()
    private lateinit var adapter: AllResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadAllAttempts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "All Quiz Results"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        adapter = AllResultsAdapter(attemptsList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadAllAttempts() {
        binding.progressBar.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().reference
            .child("AllAttempts")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    attemptsList.clear()

                    for (attemptSnapshot in snapshot.children) {
                        val attempt = attemptSnapshot.getValue(QuizAttempt::class.java)
                        attempt?.let { attemptsList.add(0, it) } // Add to beginning for newest first
                    }

                    if (attemptsList.isEmpty()) {
                        binding.tvNoResults.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.tvNoResults.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }

                    binding.tvTotalAttempts.text = "Total Attempts: ${attemptsList.size}"
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@AllResultsActivity,
                        "Failed to load results: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}