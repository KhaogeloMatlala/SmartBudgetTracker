package com.example.smartbudgettracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ExpenseListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        val loadAllBtn = findViewById<Button>(R.id.btnLoadAllExpenses)
        val results = findViewById<TextView>(R.id.tvExpenseResults)

        loadAllBtn.setOnClickListener {
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.readableDatabase

            val cursor = db.rawQuery(
                """
                SELECT date, startTime, endTime, description, category, amount, photo
                FROM expenses
                ORDER BY date DESC
                """,
                null
            )

            val output = StringBuilder()

            if (cursor.count == 0) {
                output.append("No expenses saved yet.")
                Log.d("SmartBudget", "No expenses found")
            } else {
                output.append("All Saved Expenses:\n\n")

                while (cursor.moveToNext()) {
                    output.append("Date: ${cursor.getString(0)}\n")
                    output.append("Time: ${cursor.getString(1)} - ${cursor.getString(2)}\n")
                    output.append("Description: ${cursor.getString(3)}\n")
                    output.append("Category: ${cursor.getString(4)}\n")
                    output.append("Amount: R${cursor.getDouble(5)}\n")
                    output.append("Photo: ${cursor.getString(6)}\n")
                    output.append("-------------------------\n")
                }

                Log.d("SmartBudget", "All expenses loaded")
            }

            cursor.close()
            results.text = output.toString()
        }
    }
}