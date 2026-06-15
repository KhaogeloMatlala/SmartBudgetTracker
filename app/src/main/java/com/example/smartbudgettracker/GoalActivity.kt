package com.example.smartbudgettracker

import android.app.DatePickerDialog
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class GoalActivity : AppCompatActivity() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSelectMonth: Button
    private lateinit var tvSelectedMonth: TextView
    private lateinit var etMinGoal: EditText
    private lateinit var etMaxGoal: EditText
    private lateinit var seekGoal: SeekBar
    private lateinit var tvSeekValue: TextView
    private lateinit var btnSaveGoals: Button

    private var selectedMonth: String? = null
    private val categoriesList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        // Bind views
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnSelectMonth = findViewById(R.id.btnSelectMonth)
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        etMinGoal = findViewById(R.id.etMinGoal)
        etMaxGoal = findViewById(R.id.etMaxGoal)
        seekGoal = findViewById(R.id.seekGoal)
        tvSeekValue = findViewById(R.id.tvSeekValue)
        btnSaveGoals = findViewById(R.id.btnSaveGoals)

        // Load categories into spinner
        loadCategories()

        // SeekBar updates the min goal field for quick input
        seekGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSeekValue.text = "💡 Preview: R$progress"
                etMinGoal.setText(progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Month picker
        btnSelectMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, _ ->
                    selectedMonth = String.format("%04d-%02d", year, month + 1)
                    tvSelectedMonth.text = selectedMonth
                    Log.d("SmartBudget", "Month selected: $selectedMonth")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).also {
                // Hide day picker — we only care about year/month
                it.datePicker.findViewById<android.widget.NumberPicker>(
                    resources.getIdentifier("day", "id", "android")
                )?.visibility = android.view.View.GONE
            }.show()
        }

        // Save goals
        btnSaveGoals.setOnClickListener {
            val category = spinnerCategory.selectedItem?.toString()
            val month = selectedMonth
            val minStr = etMinGoal.text.toString()
            val maxStr = etMaxGoal.text.toString()

            // Validation
            if (category == null || category.isEmpty()) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (month == null) {
                Toast.makeText(this, "Please select a month", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (minStr.isEmpty() || maxStr.isEmpty()) {
                Toast.makeText(this, "Enter both min and max goals", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val min = minStr.toDouble()
            val max = maxStr.toDouble()

            if (min >= max) {
                Toast.makeText(this, "Maximum must be greater than minimum", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to database
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.writableDatabase

            // Delete existing goals for this category + month (avoid duplicates)
            db.execSQL(
                "DELETE FROM goals WHERE category = ? AND month = ?",
                arrayOf(category, month)
            )

            // Insert new goal
            db.execSQL(
                "INSERT INTO goals (category, month, minGoal, maxGoal) VALUES (?, ?, ?, ?)",
                arrayOf(category, month, min, max)
            )

            Toast.makeText(this, "Goals saved for $category ($month)!", Toast.LENGTH_SHORT).show()
            Log.d("SmartBudget", "Goal saved: $category, $month, Min=$min, Max=$max")

            // Clear fields
            etMinGoal.text.clear()
            etMaxGoal.text.clear()
            selectedMonth = null
            tvSelectedMonth.text = "Not selected"
        }
    }

    private fun loadCategories() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT name FROM categories ORDER BY name ASC", null)

        while (cursor.moveToNext()) {
            categoriesList.add(cursor.getString(0))
        }
        cursor.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        Log.d("SmartBudget", "Loaded ${categoriesList.size} categories into goal spinner")
    }
}