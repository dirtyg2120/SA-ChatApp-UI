package com.example.myapplication.ui.authen

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // You can add registration logic here
        Toast.makeText(this, "Welcome to the Register Page", Toast.LENGTH_SHORT).show()
    }
}
