package com.example.myapplication.ui.authen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class VerifyActivity : AppCompatActivity() {

    private lateinit var btnVerify: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify)

        btnVerify = findViewById(R.id.btn_login)

        btnVerify.setOnClickListener {
            val intent = Intent(this@VerifyActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
