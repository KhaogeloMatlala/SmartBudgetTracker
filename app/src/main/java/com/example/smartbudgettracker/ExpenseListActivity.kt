package com.example.smartbudgettracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ExpenseListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        val startDate = findViewById<EditText>(R.id.etStartDate)
        val endDate = findViewById<EditText>(R.id.etEndDate)
        val filterBtn = findViewById<Button>(R.id.btnFilterExpenses)
        val results = findViewById<TextView>(R.id.tvExpenseResults)

        filterBtn.setOnClickListener {
            val start = startDate.text.toString()
            val end = endDate.text.toString()

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please enter both dates", Toast.LENGTH_SHORT).show()
                Log.d("SmartBudget", "Date filter failed: empty date fields")
            } else {
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.readableDatabase

                val cursor = db.rawQuery(
                    """
                    SELECT date, startTime, endTime, description, category, amount, photo
                    FROM expenses
                    WHERE date BETWEEN ? AND ?
                    ORDER BY date ASC
                    """,
                    arrayOf(start, end)
                )

                val output = StringBuilder()

                if (cursor.count == 0) {
                    output.append("No expenses found between $start and $end.")
                    Log.d("SmartBudget", "No expenses found for selected period")
                } else {
                    output.append("Expenses from $start to $end:\n\n")

                    while (cursor.moveToNext()) {
                        output.append("Date: ${cursor.getString(0)}\n")
                        output.append("Time: ${cursor.getString(1)} - ${cursor.getString(2)}\n")
                        output.append("Description: ${cursor.getString(3)}\n")
                        output.append("Category: ${cursor.getString(4)}\n")
                        output.append("Amount: R${cursor.getDouble(5)}\n")
                        output.append("Photo: ${cursor.getString(6)}\n")
                        output.append("-------------------------\n")
                    }

                    Log.d("SmartBudget", "Expenses loaded for selected period")
                }

                cursor.close()
                results.text = output.toString()
            }
        }
    }
}