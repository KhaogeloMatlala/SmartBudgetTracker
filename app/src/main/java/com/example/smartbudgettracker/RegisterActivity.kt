package com.example.smartbudgettracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val username = findViewById<EditText>(R.id.etRegisterUsername)
        val password = findViewById<EditText>(R.id.etRegisterPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val registerBtn = findViewById<Button>(R.id.btnRegisterUser)

        registerBtn.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()
            val confirm = confirmPassword.text.toString()

            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.writableDatabase

                db.execSQL(
                    "INSERT INTO users (username, password) VALUES (?, ?)",
                    arrayOf(user, pass)
                )

                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                Log.d("SmartBudget", "New user registered: $user")

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}