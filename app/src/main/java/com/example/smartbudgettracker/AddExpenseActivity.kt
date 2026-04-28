package com.example.smartbudgettracker

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var etDescription: EditText
    private lateinit var etCategoryExpense: EditText
    private lateinit var etAmount: EditText
    private lateinit var btnAddPhoto: Button
    private lateinit var btnSaveExpense: Button
    private lateinit var imgExpensePhoto: ImageView

    private var savedPhotoPath: String = "No photo"

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        etDate = findViewById(R.id.etDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        etDescription = findViewById(R.id.etDescription)
        etCategoryExpense = findViewById(R.id.etCategoryExpense)
        etAmount = findViewById(R.id.etAmount)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)
        imgExpensePhoto = findViewById(R.id.imgExpensePhoto)

        btnAddPhoto.setOnClickListener {
            openCamera()
        }

        btnSaveExpense.setOnClickListener {
            saveExpense()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            imgExpensePhoto.setImageBitmap(photo)
            savedPhotoPath = "Photo captured"
            Log.d("SmartBudget", "Photo captured successfully")
        }
    }

    private fun saveExpense() {
        val date = etDate.text.toString().trim()
        val startTime = etStartTime.text.toString().trim()
        val endTime = etEndTime.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val category = etCategoryExpense.text.toString().trim()
        val amount = etAmount.text.toString().trim()

        if (
            date.isEmpty() ||
            startTime.isEmpty() ||
            endTime.isEmpty() ||
            description.isEmpty() ||
            category.isEmpty() ||
            amount.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.writableDatabase

            val values = ContentValues()
            values.put("date", date)
            values.put("startTime", startTime)
            values.put("endTime", endTime)
            values.put("description", description)
            values.put("category", category)
            values.put("amount", amount.toDouble())
            values.put("photo", savedPhotoPath)

            val result = db.insert("expenses", null, values)

            if (result != -1L) {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_LONG).show()
                Log.d("SmartBudget", "Expense saved successfully")

                clearFields()

                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to save expense", Toast.LENGTH_LONG).show()
                Log.d("SmartBudget", "Failed to save expense")
            }

            db.close()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("SmartBudget", "Save expense error: ${e.message}")
        }
    }

    private fun clearFields() {
        etDate.text.clear()
        etStartTime.text.clear()
        etEndTime.text.clear()
        etDescription.text.clear()
        etCategoryExpense.text.clear()
        etAmount.text.clear()
        imgExpensePhoto.setImageDrawable(null)
        savedPhotoPath = "No photo"
    }
}