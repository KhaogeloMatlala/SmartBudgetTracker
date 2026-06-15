package com.example.smartbudgettracker

import android.R.attr.textStyle
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class CategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val categoryInput = findViewById<EditText>(R.id.etCategory)
        val saveBtn = findViewById<Button>(R.id.btnSaveCategory)
        val llCategoryList = findViewById<LinearLayout>(R.id.llCategoryList)

        // Load existing categories
        loadCategories(llCategoryList)

        saveBtn.setOnClickListener {
            val category = categoryInput.text.toString().trim()

            if (category.isEmpty()) {
                Toast.makeText(this, "Enter a category", Toast.LENGTH_SHORT).show()
            } else {
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.writableDatabase

                db.execSQL("INSERT INTO categories (name) VALUES (?)", arrayOf(category))

                Toast.makeText(this, "Category saved!", Toast.LENGTH_SHORT).show()
                Log.d("SmartBudget", "Category added: $category")
                categoryInput.text.clear()

                // Refresh list
                loadCategories(llCategoryList)
            }
        }
    }

    private fun loadCategories(container: LinearLayout) {
        container.removeAllViews()

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT id, name FROM categories ORDER BY name ASC", null)

        while (cursor.moveToNext()) {
            val name = cursor.getString(1)

            val card = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 10
                }
                radius = 12f
                cardElevation = 2f
                setCardBackgroundColor(Color.WHITE)
                setContentPadding(16, 14, 16, 14)
            }

            val textView = TextView(this).apply {
                text = "🏷️  $name"
                textSize = 16f
                setTextColor(Color.parseColor("#333333"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            card.addView(textView)
            container.addView(card)
        }
        cursor.close()
        Log.d("SmartBudget", "Categories loaded into list")
    }
}