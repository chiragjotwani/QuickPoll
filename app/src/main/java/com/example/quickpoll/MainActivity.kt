package com.example.quickpoll

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickpoll.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.content.Context

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var quizModelList: MutableList<QuizModel>
    private lateinit var adapter: QuizListAdapter
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.subtitle = "Welcome, ${auth.currentUser?.email}"

        setUpViews()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_logout -> {
                // Handle logout
                val auth = FirebaseAuth.getInstance()
                auth.signOut()

                // Clear any saved data if needed
                val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                // Navigate to login activity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            R.id.action_profile -> {
                // Handle profile action
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUserProfile() {
        val user = auth.currentUser
        val message = """
            Email: ${user?.email}
            User ID: ${user?.uid}
            Email Verified: ${user?.isEmailVerified}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Profile")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Send Verification") { _, _ ->
                sendEmailVerification()
            }
            .show()
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        if (user?.isEmailVerified == true) {
            Toast.makeText(this, "Email already verified", Toast.LENGTH_SHORT).show()
            return
        }

        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Verification email sent to ${user.email}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send verification email: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setUpViews() {
        quizModelList = mutableListOf()
        setupRecyclerView()
        getDataFromFirebase()
    }

    private fun setupRecyclerView() {
        adapter = QuizListAdapter(quizModelList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE
        Log.d(TAG, "Starting to fetch data from Firebase at 'Quizzes' path...")

        FirebaseDatabase.getInstance().reference.child("Quizzes").get()
            .addOnSuccessListener { dataSnapshot ->
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "Data fetch successful. Snapshot exists: ${dataSnapshot.exists()}")

                if (!dataSnapshot.exists()) {
                    Log.w(TAG, "DataSnapshot at 'Quizzes' path doesn't exist.")
                    Toast.makeText(this, "Quiz data not found. Check Firebase path.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val freshQuizList = mutableListOf<QuizModel>()
                var parsingErrors = 0
                for (snapshot in dataSnapshot.children) {
                    try {
                        val quizModel = snapshot.getValue(QuizModel::class.java)
                        if (quizModel != null) {
                            freshQuizList.add(quizModel)
                        } else {
                            parsingErrors++
                            Log.w(TAG, "A quiz entry was found but could not be parsed. Check data structure for key: ${snapshot.key}")
                        }
                    } catch (e: Exception) {
                        parsingErrors++
                        Log.e(TAG, "Exception parsing quiz entry for key: ${snapshot.key}", e)
                    }
                }

                Log.d(TAG, "Total quizzes parsed: ${freshQuizList.size}. Parsing errors: $parsingErrors")

                if (freshQuizList.isEmpty()) {
                    if (parsingErrors > 0) {
                        Toast.makeText(this, "Found quiz data, but failed to parse. Check data structure in Firebase.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "No quizzes found in the database.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    quizModelList.clear()
                    quizModelList.addAll(freshQuizList)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "Error fetching data from Firebase", exception)
                Toast.makeText(
                    this,
                    "Failed to load quizzes: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}