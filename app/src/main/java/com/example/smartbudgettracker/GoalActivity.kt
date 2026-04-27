package com.example.smartbudgettracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GoalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        val minGoal = findViewById<EditText>(R.id.etMinGoal)
        val maxGoal = findViewById<EditText>(R.id.etMaxGoal)
        val seekGoal = findViewById<SeekBar>(R.id.seekGoal)
        val seekValue = findViewById<TextView>(R.id.tvSeekValue)
        val saveGoals = findViewById<Button>(R.id.btnSaveGoals)

        seekGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekValue.text = "Goal preview: R$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveGoals.setOnClickListener {
            val min = minGoal.text.toString()
            val max = maxGoal.text.toString()

            if (min.isEmpty() || max.isEmpty()) {
                Toast.makeText(this, "Enter both goals", Toast.LENGTH_SHORT).show()
            } else if (min.toDouble() >= max.toDouble()) {
                Toast.makeText(this, "Maximum must be greater than minimum", Toast.LENGTH_SHORT).show()
            } else {
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.writableDatabase

                db.execSQL("DELETE FROM goals")

                val sql = "INSERT INTO goals (minGoal, maxGoal) VALUES (?, ?)"

                db.execSQL(
                    sql,
                    arrayOf(
                        min.toDouble(),
                        max.toDouble()
                    )
                )

                Toast.makeText(this, "Goals saved to database!", Toast.LENGTH_SHORT).show()

                minGoal.text.clear()
                maxGoal.text.clear()
            }
        }
    }
}