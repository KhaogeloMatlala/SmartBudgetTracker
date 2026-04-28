package com.example.smartbudgettracker

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val registerText = findViewById<TextView>(R.id.tvRegister)

        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginBtn.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show()
            } else {
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.readableDatabase

                val cursor: Cursor = db.rawQuery(
                    "SELECT * FROM users WHERE username = ? AND password = ?",
                    arrayOf(user, pass)
                )

                if (cursor.count > 0) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    Log.d("SmartBudget", "User logged in: $user")

                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid login details", Toast.LENGTH_SHORT).show()
                }

                cursor.close()
            }
        }
    }
}