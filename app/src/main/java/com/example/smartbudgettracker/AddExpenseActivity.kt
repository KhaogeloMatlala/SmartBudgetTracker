package com.example.smartbudgettracker

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class AddExpenseActivity : AppCompatActivity() {

    private var savedPhotoPath: String = "No photo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Input fields
        val date = findViewById<EditText>(R.id.etDate)
        val startTime = findViewById<EditText>(R.id.etStartTime)
        val endTime = findViewById<EditText>(R.id.etEndTime)
        val description = findViewById<EditText>(R.id.etDescription)
        val category = findViewById<EditText>(R.id.etCategoryExpense)
        val amount = findViewById<EditText>(R.id.etAmount)

        // Buttons
        val addPhoto = findViewById<Button>(R.id.btnAddPhoto)
        val saveExpense = findViewById<Button>(R.id.btnSaveExpense)

        // Image preview
        val photoPreview = findViewById<ImageView>(R.id.imgExpensePhoto)

        // Camera launcher
        val cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->

                if (bitmap != null) {
                    photoPreview.setImageBitmap(bitmap)

                    // Save image internally
                    savedPhotoPath = saveBitmapToInternalStorage(bitmap)

                    Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show()
                    Log.d("SmartBudget", "Photo saved at: $savedPhotoPath")
                } else {
                    Toast.makeText(this, "No photo taken", Toast.LENGTH_SHORT).show()
                    Log.d("SmartBudget", "User cancelled photo capture")
                }
            }

        // Open camera
        addPhoto.setOnClickListener {
            cameraLauncher.launch(null)
        }

        // Save expense
        saveExpense.setOnClickListener {

            if (
                date.text.toString().isEmpty() ||
                startTime.text.toString().isEmpty() ||
                endTime.text.toString().isEmpty() ||
                description.text.toString().isEmpty() ||
                category.text.toString().isEmpty() ||
                amount.text.toString().isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                Log.d("SmartBudget", "Validation failed: Empty fields")
            } else {

                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.writableDatabase

                val sql = """
                    INSERT INTO expenses
                    (date, startTime, endTime, description, category, amount, photo)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """

                db.execSQL(
                    sql,
                    arrayOf(
                        date.text.toString(),
                        startTime.text.toString(),
                        endTime.text.toString(),
                        description.text.toString(),
                        category.text.toString(),
                        amount.text.toString().toDouble(),
                        savedPhotoPath
                    )
                )

                Toast.makeText(this, "Expense saved to database!", Toast.LENGTH_SHORT).show()
                Log.d("SmartBudget", "Expense saved successfully")

                // Clear fields
                date.text.clear()
                startTime.text.clear()
                endTime.text.clear()
                description.text.clear()
                category.text.clear()
                amount.text.clear()
                photoPreview.setImageDrawable(null)

                savedPhotoPath = "No photo"
            }
        }
    }

    // Save bitmap to internal storage
    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String {

        val fileName = "expense_photo_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, fileName)

        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        outputStream.flush()
        outputStream.close()

        return file.absolutePath
    }
}