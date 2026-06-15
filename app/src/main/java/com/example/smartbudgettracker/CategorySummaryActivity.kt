package com.example.smartbudgettracker

import android.app.DatePickerDialog
import android.database.Cursor
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Calendar

class CategorySummaryActivity : AppCompatActivity() {

    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var tvSelectedStartDate: TextView
    private lateinit var tvSelectedEndDate: TextView
    private lateinit var btnShowGraph: Button
    private lateinit var barChart: BarChart
    private lateinit var tvGraphSummary: TextView

    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_summary)

        // Bind views
        btnStartDate = findViewById(R.id.btnStartDate)
        btnEndDate = findViewById(R.id.btnEndDate)
        tvSelectedStartDate = findViewById(R.id.tvSelectedStartDate)
        tvSelectedEndDate = findViewById(R.id.tvSelectedEndDate)
        btnShowGraph = findViewById(R.id.btnShowGraph)
        barChart = findViewById(R.id.barChart)
        tvGraphSummary = findViewById(R.id.tvGraphSummary)

        // Start date picker
        btnStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedStartDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                    tvSelectedStartDate.text = selectedStartDate
                    Log.d("SmartBudget", "Start date selected: $selectedStartDate")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // End date picker
        btnEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedEndDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                    tvSelectedEndDate.text = selectedEndDate
                    Log.d("SmartBudget", "End date selected: $selectedEndDate")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Show graph button
        btnShowGraph.setOnClickListener {
            if (selectedStartDate == null || selectedEndDate == null) {
                Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
                Log.d("SmartBudget", "Graph failed: missing dates")
            } else {
                loadGraph(selectedStartDate!!, selectedEndDate!!)
                Log.d("SmartBudget", "Graph loaded for $selectedStartDate to $selectedEndDate")
            }
        }
    }

    private fun loadGraph(start: String, end: String) {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // 1. Get spending per category for the date range
        val cursor: Cursor = db.rawQuery(
            """
            SELECT category, SUM(amount) 
            FROM expenses 
            WHERE date BETWEEN ? AND ? 
            GROUP BY category 
            ORDER BY category ASC
            """,
            arrayOf(start, end)
        )

        val categories = mutableListOf<String>()
        val amounts = mutableListOf<Float>()
        var grandTotal = 0.0

        while (cursor.moveToNext()) {
            val cat = cursor.getString(0)
            val amt = cursor.getDouble(1)
            categories.add(cat)
            amounts.add(amt.toFloat())
            grandTotal += amt
        }
        cursor.close()

        if (categories.isEmpty()) {
            tvGraphSummary.text = "No expenses found between $start and $end."
            barChart.clear()
            return
        }

        // 2. Get min/max goals for these categories (latest month that overlaps the range)
        // Extract year-month from the start date (e.g., "2026-04" from "2026-04-01")
        val month = start.substring(0, 7) // "YYYY-MM"

        val goalCursor: Cursor = db.rawQuery(
            """
            SELECT category, minGoal, maxGoal 
            FROM goals 
            WHERE month = ? AND category IN (${categories.joinToString(",") { "'$it'" }})
            """,
            arrayOf(month)
        )

        val minGoals = mutableMapOf<String, Float>()
        val maxGoals = mutableMapOf<String, Float>()

        while (goalCursor.moveToNext()) {
            val cat = goalCursor.getString(0)
            val min = goalCursor.getDouble(1).toFloat()
            val max = goalCursor.getDouble(2).toFloat()
            minGoals[cat] = min
            maxGoals[cat] = max
        }
        goalCursor.close()

        // 3. Build bar chart entries
        val entries = mutableListOf<BarEntry>()
        for (i in categories.indices) {
            entries.add(BarEntry(i.toFloat(), amounts[i]))
        }

        // 4. Configure bar chart
        val dataSet = BarDataSet(entries, "Spending per Category").apply {
            color = Color.parseColor("#0B7A34")
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        // 5. Add min/max goal lines
        barChart.axisLeft.apply {
            // Remove any existing limit lines
            removeAllLimitLines()

            // Calculate average min and max goals across categories (for display)
            val avgMin = if (minGoals.isNotEmpty()) minGoals.values.average().toFloat() else 0f
            val avgMax = if (maxGoals.isNotEmpty()) maxGoals.values.average().toFloat() else 0f

            if (avgMin > 0f) {
                val minLine = LimitLine(avgMin, "Min Goal")
                minLine.lineWidth = 2f
                minLine.lineColor = Color.parseColor("#F9A825")
                minLine.textColor = Color.parseColor("#F9A825")
                minLine.textSize = 11f
                minLine.enableDashedLine(10f, 10f, 0f)
                addLimitLine(minLine)
            }

            if (avgMax > 0f) {
                val maxLine = LimitLine(avgMax, "Max Goal")
                maxLine.lineWidth = 2f
                maxLine.lineColor = Color.RED
                maxLine.textColor = Color.RED
                maxLine.textSize = 11f
                maxLine.enableDashedLine(10f, 10f, 0f)
                addLimitLine(maxLine)
            }

            axisMinimum = 0f
        }

        // 6. Configure X-axis
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(categories)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            labelCount = categories.size
            textSize = 11f
            labelRotationAngle = -30f
        }

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.textSize = 12f
        barChart.animateY(1000)

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        barChart.data = barData
        barChart.invalidate()

        // 7. Update text summary
        val summaryBuilder = StringBuilder()
        summaryBuilder.append("📊 Total spent: R${String.format("%.2f", grandTotal)}\n\n")
        for (i in categories.indices) {
            val cat = categories[i]
            val amt = amounts[i]
            val min = minGoals[cat]
            val max = maxGoals[cat]

            summaryBuilder.append("$cat: R${String.format("%.2f", amt)}")
            if (min != null && max != null) {
                val status = when {
                    amt < min -> " ⚠️ Under min"
                    amt > max -> " 🔴 Over max"
                    else -> " ✅ Within range"
                }
                summaryBuilder.append(status)
            }
            summaryBuilder.append("\n")
        }
        tvGraphSummary.text = summaryBuilder.toString()

        Log.d("SmartBudget", "Graph rendered with ${categories.size} categories")
    }
}