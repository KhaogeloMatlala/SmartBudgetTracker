package com.example.smartbudgettracker

import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ExpenseListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        val btnRefresh = findViewById<Button>(R.id.btnRefreshExpenses)
        val llExpenseList = findViewById<LinearLayout>(R.id.llExpenseList)

        // Auto-load on open
        loadExpenses(llExpenseList)

        btnRefresh.setOnClickListener {
            loadExpenses(llExpenseList)
        }
    }

    private fun loadExpenses(container: LinearLayout) {
        container.removeAllViews()

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery(
            "SELECT date, startTime, endTime, description, category, amount, photo FROM expenses ORDER BY date DESC",
            null
        )

        if (cursor.count == 0) {
            val emptyText = TextView(this)
            emptyText.text = "💡 No expenses saved yet."
            emptyText.textSize = 16f
            emptyText.setTextColor(Color.parseColor("#999999"))
            emptyText.setPadding(0, 40, 0, 0)
            emptyText.gravity = android.view.Gravity.CENTER
            container.addView(emptyText)
            Log.d("SmartBudget", "No expenses found")
        } else {
            while (cursor.moveToNext()) {
                val date = cursor.getString(0)
                val start = cursor.getString(1)
                val end = cursor.getString(2)
                val desc = cursor.getString(3)
                val cat = cursor.getString(4)
                val amount = cursor.getDouble(5)

                val card = CardView(this)
                card.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 12
                }
                card.radius = 14f
                card.cardElevation = 3f
                card.setCardBackgroundColor(Color.WHITE)
                card.setContentPadding(18, 14, 18, 14)

                val innerLayout = LinearLayout(this)
                innerLayout.orientation = LinearLayout.VERTICAL

                val tvTop = TextView(this)
                tvTop.text = "$cat  •  $date"
                tvTop.textSize = 14f
                tvTop.setTextColor(Color.parseColor("#0B7A34"))
                tvTop.setTypeface(tvTop.typeface, android.graphics.Typeface.BOLD)
                innerLayout.addView(tvTop)

                val tvDesc = TextView(this)
                tvDesc.text = desc
                tvDesc.textSize = 15f
                tvDesc.setTextColor(Color.parseColor("#333333"))
                tvDesc.setPadding(0, 4, 0, 4)
                innerLayout.addView(tvDesc)

                val tvBottom = TextView(this)
                tvBottom.text = "⏰ $start - $end   |   💵 R${String.format("%.2f", amount)}"
                tvBottom.textSize = 13f
                tvBottom.setTextColor(Color.parseColor("#666666"))
                innerLayout.addView(tvBottom)

                card.addView(innerLayout)
                container.addView(card)
            }
        }
        cursor.close()
        Log.d("SmartBudget", "Expenses loaded")
    }
}