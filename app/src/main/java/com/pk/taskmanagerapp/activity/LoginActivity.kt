package com.pk.taskmanagerapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pk.taskmanagerapp.R
import com.pk.taskmanagerapp.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var repository: TaskRepository
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etName: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvSwitchToRegister: TextView
    private lateinit var tvSwitchToLogin: TextView

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        repository = TaskRepository()

        // Check if user is already logged in
        if (repository.isUserLoggedIn()) {
            startMainActivity()
            return
        }

        initViews()
        setupClickListeners()
        showLoginForm() // Start with login form
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etName = findViewById(R.id.etName)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        tvSwitchToRegister = findViewById(R.id.tvSwitchToRegister)
        tvSwitchToLogin = findViewById(R.id.tvSwitchToLogin)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please enter email and password")
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val name = etName.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                showToast("Please enter all fields")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showToast("Password should be at least 6 characters")
                return@setOnClickListener
            }

            registerUser(email, password, name)
        }

        tvSwitchToRegister.setOnClickListener {
            showRegisterForm()
        }

        tvSwitchToLogin.setOnClickListener {
            showLoginForm()
        }
    }

    private fun showLoginForm() {
        isLoginMode = true
        etName.visibility = TextView.GONE
        btnLogin.visibility = Button.VISIBLE
        btnRegister.visibility = Button.GONE
        tvSwitchToRegister.visibility = TextView.VISIBLE
        tvSwitchToLogin.visibility = TextView.GONE
    }

    private fun showRegisterForm() {
        isLoginMode = false
        etName.visibility = TextView.VISIBLE
        btnLogin.visibility = Button.GONE
        btnRegister.visibility = Button.VISIBLE
        tvSwitchToRegister.visibility = TextView.GONE
        tvSwitchToLogin.visibility = TextView.VISIBLE
    }

    private fun loginUser(email: String, password: String) {
        showToast("Logging in...")

        CoroutineScope(Dispatchers.IO).launch {
            val success = repository.loginUser(email, password)
            withContext(Dispatchers.Main) {
                if (success) {
                    showToast("Login successful!")
                    startMainActivity()
                } else {
                    showToast("Login failed. Check your credentials or register first.")
                }
            }
        }
    }

    private fun registerUser(email: String, password: String, name: String) {
        showToast("Creating your account...")

        CoroutineScope(Dispatchers.IO).launch {
            val success = repository.registerUser(email, password, name)
            withContext(Dispatchers.Main) {
                if (success) {
                    showToast("Account created successfully!")
                    startMainActivity()
                } else {
                    showToast("Registration failed. Email might be already in use.")
                }
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}