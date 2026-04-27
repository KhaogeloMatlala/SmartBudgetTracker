package com.example.smartbudgettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnCategory = findViewById<Button>(R.id.btnCategory)
        val btnExpense = findViewById<Button>(R.id.btnExpense)
        val btnGoals = findViewById<Button>(R.id.btnGoals)
        val btnViewExpenses = findViewById<Button>(R.id.btnViewExpenses)
        val btnSummary = findViewById<Button>(R.id.btnSummary)

        btnCategory.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        btnExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        btnGoals.setOnClickListener {
            startActivity(Intent(this, GoalActivity::class.java))
        }

        btnViewExpenses.setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }

        btnSummary.setOnClickListener {
            startActivity(Intent(this, CategorySummaryActivity::class.java))
        }
    }
}