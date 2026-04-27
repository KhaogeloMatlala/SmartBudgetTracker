package com.example.smartbudgettracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategorySummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_summary)

        // Input fields for the selected date range
        val startDate = findViewById<EditText>(R.id.etSummaryStartDate)
        val endDate = findViewById<EditText>(R.id.etSummaryEndDate)

        // Button and result display
        val showSummary = findViewById<Button>(R.id.btnShowSummary)
        val results = findViewById<TextView>(R.id.tvSummaryResults)

        showSummary.setOnClickListener {
            val start = startDate.text.toString()
            val end = endDate.text.toString()

            // Validate that both date fields are entered
            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Enter start and end date", Toast.LENGTH_SHORT).show()
                Log.d("SmartBudget", "Category summary failed: empty date fields")
            } else {
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.readableDatabase

                // Calculate total spending per category within the selected date range
                val cursor = db.rawQuery(
                    """
                    SELECT category, SUM(amount) 
                    FROM expenses 
                    WHERE date BETWEEN ? AND ? 
                    GROUP BY category
                    ORDER BY category ASC
                    """,
                    arrayOf(start, end)
                )

                val output = StringBuilder()
                var grandTotal = 0.0

                if (cursor.count == 0) {
                    output.append("No category totals found between $start and $end.")
                    Log.d("SmartBudget", "No category totals found for selected period")
                } else {
                    output.append("Category totals from $start to $end:\n\n")

                    while (cursor.moveToNext()) {
                        val category = cursor.getString(0)
                        val total = cursor.getDouble(1)

                        output.append("$category: R$total\n")
                        grandTotal += total
                    }

                    output.append("\nTotal spent: R$grandTotal")
                    Log.d("SmartBudget", "Category summary loaded successfully")
                }

                cursor.close()
                results.text = output.toString()
            }
        }
    }
}