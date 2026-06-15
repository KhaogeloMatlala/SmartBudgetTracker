package com.example.smartbudgettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Navigation buttons
        val btnCategory = findViewById<Button>(R.id.btnCategory)
        val btnExpense = findViewById<Button>(R.id.btnExpense)
        val btnGoals = findViewById<Button>(R.id.btnGoals)
        val btnViewExpenses = findViewById<Button>(R.id.btnViewExpenses)
        val btnSummary = findViewById<Button>(R.id.btnSummary)
        val btnBudgetHealth = findViewById<Button>(R.id.btnBudgetHealth)
        val btnBadges = findViewById<Button>(R.id.btnBadges)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Financial summary card views
        val tvTotalSpent = findViewById<TextView>(R.id.tvDashboardTotalSpent)
        val progressBudget = findViewById<ProgressBar>(R.id.progressDashboardBudget)
        val tvHealth = findViewById<TextView>(R.id.tvDashboardHealth)
        val summaryCard = findViewById<CardView>(R.id.cardDashboardSummary)

        // Load financial data into card
        loadDashboardSummary(tvTotalSpent, progressBudget, tvHealth)

        // Make card clickable to go to Budget Health
        summaryCard.setOnClickListener {
            startActivity(Intent(this, BudgetHealthActivity::class.java))
        }

        // Button click listeners
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

        btnBudgetHealth.setOnClickListener {
            startActivity(Intent(this, BudgetHealthActivity::class.java))
        }

        btnBadges.setOnClickListener {
            startActivity(Intent(this, BadgesActivity::class.java))
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadDashboardSummary(
        tvTotalSpent: TextView,
        progressBudget: ProgressBar,
        tvHealth: TextView
    ) {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // Calculate previous month
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MONTH, -1)
        val previousMonth = String.format(
            "%04d-%02d",
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1
        )
        val monthStart = "$previousMonth-01"
        val monthEnd = "$previousMonth-31"

        // Total spent last month
        val cursor = db.rawQuery(
            "SELECT SUM(amount) FROM expenses WHERE date BETWEEN ? AND ?",
            arrayOf(monthStart, monthEnd)
        )
        cursor.moveToFirst()
        val totalSpent = cursor.getDouble(0)
        cursor.close()

        // Total max goal for last month
        val goalCursor = db.rawQuery(
            "SELECT SUM(maxGoal) FROM goals WHERE month = ?",
            arrayOf(previousMonth)
        )
        goalCursor.moveToFirst()
        val totalMax = goalCursor.getDouble(0)
        goalCursor.close()

        tvTotalSpent.text = "Spent last month: R${String.format("%.2f", totalSpent)}"

        if (totalMax > 0) {
            val percent = ((totalSpent / totalMax) * 100).toInt().coerceIn(0, 100)
            progressBudget.progress = percent
            progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(
                if (percent > 100) android.graphics.Color.parseColor("#C62828")
                else if (percent >= 80) android.graphics.Color.parseColor("#F9A825")
                else android.graphics.Color.parseColor("#0B7A34")
            )

            tvHealth.text = when {
                percent > 100 -> "🔴 Over budget by ${percent - 100}%"
                percent >= 80 -> "🟡 Approaching limit"
                else -> "✅ Well within budget"
            }
        } else {
            progressBudget.progress = 0
            tvHealth.text = "⚠️ No goals set for last month"
        }
    }
}