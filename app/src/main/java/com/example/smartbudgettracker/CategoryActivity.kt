package com.example.smartbudgettracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val categoryInput = findViewById<EditText>(R.id.etCategory)
        val saveBtn = findViewById<Button>(R.id.btnSaveCategory)

        saveBtn.setOnClickListener {
            val category = categoryInput.text.toString()

            if (category.isEmpty()) {
                Toast.makeText(this, "Enter a category", Toast.LENGTH_SHORT).show()

            } else {
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.writableDatabase

                val sql = "INSERT INTO categories (name) VALUES (?)"
                db.execSQL(sql, arrayOf(category))

                Toast.makeText(this, "Category saved to database!", Toast.LENGTH_SHORT).show()
                categoryInput.text.clear()
            }
        }
    }
}