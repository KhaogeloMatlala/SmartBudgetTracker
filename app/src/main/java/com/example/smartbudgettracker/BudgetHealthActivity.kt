package com.example.smartbudgettracker

import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class BudgetHealthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_health)

        val progressBudget = findViewById<ProgressBar>(R.id.progressBudget)
        val tvBudgetMonth = findViewById<TextView>(R.id.tvBudgetMonth)
        val tvBudgetStatus = findViewById<TextView>(R.id.tvBudgetStatus)
        val tvBudgetDetails = findViewById<TextView>(R.id.tvBudgetDetails)
        val llCategoryHealth = findViewById<LinearLayout>(R.id.llCategoryHealth)

        // Calculate previous month (YYYY-MM)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val previousMonth = String.format("%04d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        val monthStart = "$previousMonth-01"
        val monthEnd = "$previousMonth-31"

        tvBudgetMonth.text = "Previous Month: $previousMonth"

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // 1. Get total spending per category for previous month
        val cursor: Cursor = db.rawQuery(
            """
            SELECT category, SUM(amount) 
            FROM expenses 
            WHERE date BETWEEN ? AND ? 
            GROUP BY category
            """,
            arrayOf(monthStart, monthEnd)
        )

        val categorySpending = mutableMapOf<String, Double>()
        var totalSpent = 0.0

        while (cursor.moveToNext()) {
            val cat = cursor.getString(0)
            val amt = cursor.getDouble(1)
            categorySpending[cat] = amt
            totalSpent += amt
        }
        cursor.close()

        // 2. Get goals for previous month
        val goalCursor: Cursor = db.rawQuery(
            "SELECT category, minGoal, maxGoal FROM goals WHERE month = ?",
            arrayOf(previousMonth)
        )

        val categoryGoals = mutableMapOf<String, Pair<Double, Double>>() // category -> (min, max)
        var totalMin = 0.0
        var totalMax = 0.0

        while (goalCursor.moveToNext()) {
            val cat = goalCursor.getString(0)
            val min = goalCursor.getDouble(1)
            val max = goalCursor.getDouble(2)
            categoryGoals[cat] = Pair(min, max)
            totalMin += min
            totalMax += max
        }
        goalCursor.close()

        // 3. Calculate budget health percentage
        // If totalMax is 0, no goals set
        val healthPercent = if (totalMax > 0) {
            ((totalSpent / totalMax) * 100).toInt().coerceIn(0, 100)
        } else {
            -1
        }

        // 4. Determine status
        val status: String
        val statusColor: String

        when {
            healthPercent == -1 -> {
                status = "⚠️ No Goals Set"
                statusColor = "#F9A825"
                progressBudget.progress = 0
            }
            totalSpent < totalMin -> {
                status = "⬇️ Under Minimum"
                statusColor = "#F9A825"
                progressBudget.progress = healthPercent
            }
            totalSpent > totalMax -> {
                status = "🔴 Over Budget!"
                statusColor = "#C62828"
                progressBudget.progress = 100
            }
            else -> {
                status = "✅ On Track!"
                statusColor = "#0B7A34"
                progressBudget.progress = healthPercent
            }
        }

        tvBudgetStatus.text = status
        tvBudgetStatus.setTextColor(Color.parseColor(statusColor))

        // 5. Details text
        val details = StringBuilder()
        details.append("Total Spent: R${String.format("%.2f", totalSpent)}\n")
        if (totalMin > 0) details.append("Min Goal Total: R${String.format("%.2f", totalMin)}\n")
        if (totalMax > 0) details.append("Max Goal Total: R${String.format("%.2f", totalMax)}\n")
        if (totalMax > 0) details.append("Budget Used: $healthPercent%")
        tvBudgetDetails.text = details.toString()

        // 6. Per-category health bars
        for ((cat, spent) in categorySpending) {
            val goal = categoryGoals[cat]
            val minGoal = goal?.first ?: 0.0
            val maxGoal = goal?.second ?: 0.0

            // Create row for this category
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 8, 0, 16)
            }

            val catLabel = TextView(this).apply {
                text = "$cat: R${String.format("%.2f", spent)}"
                textSize = 15f
                setTextColor(Color.parseColor("#333333"))
            }
            row.addView(catLabel)

            // Progress bar for this category
            val progressMax = if (maxGoal > 0) maxGoal.toInt() else 100
            val catProgress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    30
                )
                max = progressMax
                progress = spent.toInt().coerceAtMost(progressMax)
                progressTintList = when {
                    maxGoal == 0.0 -> android.content.res.ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
                    spent > maxGoal -> android.content.res.ColorStateList.valueOf(Color.parseColor("#C62828"))
                    spent < minGoal -> android.content.res.ColorStateList.valueOf(Color.parseColor("#F9A825"))
                    else -> android.content.res.ColorStateList.valueOf(Color.parseColor("#0B7A34"))
                }
            }
            row.addView(catProgress)

            val catStatus = TextView(this).apply {
                text = when {
                    maxGoal == 0.0 -> "No goal set"
                    spent > maxGoal -> "🔴 Over max (R${String.format("%.2f", maxGoal)})"
                    spent < minGoal -> "⚠️ Under min (R${String.format("%.2f", minGoal)})"
                    else -> "✅ Within range (R${String.format("%.2f", minGoal)} - R${String.format("%.2f", maxGoal)})"
                }
                textSize = 13f
                setTextColor(Color.parseColor("#666666"))
            }
            row.addView(catStatus)

            llCategoryHealth.addView(row)
        }

        if (categorySpending.isEmpty()) {
            val noData = TextView(this).apply {
                text = "No expenses recorded for $previousMonth."
                textSize = 15f
                setTextColor(Color.parseColor("#999999"))
                gravity = Gravity.CENTER
            }
            llCategoryHealth.addView(noData)
        }

        Log.d("SmartBudget", "Budget health loaded for $previousMonth: $status ($healthPercent%)")
    }
}