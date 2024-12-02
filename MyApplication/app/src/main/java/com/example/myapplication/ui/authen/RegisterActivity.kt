package com.example.myapplication.ui.authen

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.api.ApiRepository
import com.example.myapplication.api.RetrofitInstance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etDob: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button

    private val apiRepository = ApiRepository(RetrofitInstance.apiService)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // Initialize views
        etUsername = findViewById(R.id.et_username_register)
        etPhone = findViewById(R.id.et_phone_register)
        etEmail = findViewById(R.id.et_email_register)
        etDob = findViewById(R.id.et_dob_register)
        etPassword = findViewById(R.id.et_password_register)
        btnRegister = findViewById(R.id.btn_register_register)
        
        // Set up DatePicker for Date of Birth field
        etDob.setOnClickListener {
            showDatePickerDialog()
        }

        // Set up button click listener
        btnRegister.setOnClickListener {
            // Collect input data
            val username = etUsername.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val dob = etDob.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || phone.isEmpty() || email.isEmpty() || dob.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(username, phone, email, dob, password)
            }
        }
    }

    // Function to show DatePickerDialog
    private fun showDatePickerDialog() {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Create the DatePickerDialog
    val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay)
            }.time
        )
        etDob.setText(formattedDate)
    }, year, month, day)
    datePickerDialog.show()
}

    // Function to register the user
    private fun registerUser(username: String, phone: String, email: String, dob: String, password: String) {
        val cookie = "JSESSIONID=A131F42C8102F93E213C7AA016E31DA2"

        lifecycleScope.launch {
            try {
                val response = apiRepository.signUp(cookie, username, phone, password, dob, email)

                if (response.success) {
                    Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Registration failed, try again", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
