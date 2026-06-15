package com.example.smartbudgettracker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var etDescription: EditText
    private lateinit var etCategoryExpense: EditText
    private lateinit var etAmount: EditText
    private lateinit var btnAddPhoto: Button
    private lateinit var btnChooseGallery: Button
    private lateinit var btnSaveExpense: Button
    private lateinit var imgExpensePhoto: ImageView

    private var savedPhotoPath: String = "No photo"
    private var currentPhotoUri: Uri? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val GALLERY_REQUEST_CODE = 200
        private const val CAMERA_PERMISSION_CODE = 101
        private const val NOTIFICATION_PERMISSION_CODE = 102
        private const val CHANNEL_ID = "spending_alerts"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Bind views
        etDate = findViewById(R.id.etDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        etDescription = findViewById(R.id.etDescription)
        etCategoryExpense = findViewById(R.id.etCategoryExpense)
        etAmount = findViewById(R.id.etAmount)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnChooseGallery = findViewById(R.id.btnChooseGallery)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)
        imgExpensePhoto = findViewById(R.id.imgExpensePhoto)

        // Create notification channel
        createNotificationChannel()

        // Camera button
        btnAddPhoto.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        // Gallery button
        btnChooseGallery.setOnClickListener {
            openGallery()
        }

        // Save button
        btnSaveExpense.setOnClickListener {
            saveExpense()
        }

        // Request notification permission for Android 13+
        requestNotificationPermissionIfNeeded()
    }

    // ─── CAMERA ────────────────────────────────────────
    private fun checkCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        Log.d("SmartBudget", "Camera opened")
    }

    // ─── GALLERY ───────────────────────────────────────
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        Log.d("SmartBudget", "Gallery opened")
    }

    // ─── RESULT HANDLING ──────────────────────────────
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    val photo = data?.extras?.get("data") as? Bitmap
                    if (photo != null) {
                        imgExpensePhoto.setImageBitmap(photo)
                        savedPhotoPath = saveBitmapToFile(photo)
                        Log.d("SmartBudget", "Photo captured and saved to $savedPhotoPath")
                    }
                }
            }
            GALLERY_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && data?.data != null) {
                    val imageUri: Uri = data.data!!
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        imgExpensePhoto.setImageBitmap(bitmap)
                        savedPhotoPath = saveBitmapToFile(bitmap)
                        Log.d("SmartBudget", "Gallery image loaded and saved to $savedPhotoPath")
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                        Log.e("SmartBudget", "Gallery load error: ${e.message}")
                    }
                }
            }
        }
    }

    // ─── SAVE BITMAP TO INTERNAL STORAGE ──────────────
    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "expense_${System.currentTimeMillis()}.jpg"
        val file = File(directory, fileName)

        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
        }

        return file.absolutePath
    }

    // ─── SAVE EXPENSE + CHECK BUDGET ALERT ────────────
    private fun saveExpense() {
        val date = etDate.text.toString().trim()
        val startTime = etStartTime.text.toString().trim()
        val endTime = etEndTime.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val category = etCategoryExpense.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()

        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() ||
            description.isEmpty() || category.isEmpty() || amountStr.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put("date", date)
                put("startTime", startTime)
                put("endTime", endTime)
                put("description", description)
                put("category", category)
                put("amount", amount)
                put("photo", savedPhotoPath)
            }

            val result = db.insert("expenses", null, values)

            if (result != -1L) {
                Toast.makeText(this, "Expense saved!", Toast.LENGTH_LONG).show()
                Log.d("SmartBudget", "Expense saved: $category R$amount")

                // Check budget and send alert if needed
                checkBudgetAndAlert(category, date)

                clearFields()
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to save expense", Toast.LENGTH_LONG).show()
                Log.e("SmartBudget", "Insert returned -1")
            }

            db.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("SmartBudget", "Save expense error: ${e.message}")
        }
    }

    // ─── BUDGET CHECK & NOTIFICATION ──────────────────
    private fun checkBudgetAndAlert(category: String, dateStr: String) {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // Extract month from date (YYYY-MM)
        val month = if (dateStr.length >= 7) dateStr.substring(0, 7) else return

        // Total spent in that category for the month
        val cursor = db.rawQuery(
            "SELECT SUM(amount) FROM expenses WHERE category = ? AND date LIKE ?",
            arrayOf(category, "$month%")
        )
        cursor.moveToFirst()
        val totalSpent = cursor.getDouble(0)
        cursor.close()

        // Get goal for that category + month
        val goalCursor = db.rawQuery(
            "SELECT minGoal, maxGoal FROM goals WHERE category = ? AND month = ?",
            arrayOf(category, month)
        )

        if (goalCursor.moveToFirst()) {
            val minGoal = goalCursor.getDouble(0)
            val maxGoal = goalCursor.getDouble(1)

            when {
                totalSpent >= maxGoal -> {
                    sendNotification(
                        "🚨 Over Budget!",
                        "You've exceeded your max goal of R$maxGoal for $category this month. Spent: R$totalSpent"
                    )
                }
                totalSpent >= (maxGoal * 0.8) -> {
                    sendNotification(
                        "⚠️ Approaching Limit",
                        "You've used 80%+ of your $category budget (R$totalSpent of R$maxGoal)"
                    )
                }
            }
        }
        goalCursor.close()
    }

    // ─── NOTIFICATION ─────────────────────────────────
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Spending Alerts"
            val descriptionText = "Alerts when approaching or exceeding budget"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        try {
            val manager = getSystemService(NotificationManager::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Spending Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            manager.notify(title.hashCode(), builder.build())
            Log.d("SmartBudget", "Notification sent: $title")
        } catch (e: Exception) {
            Log.e("SmartBudget", "Notification error: ${e.message}")
        }
    }

    // ─── CLEAR FIELDS ─────────────────────────────────
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

    // ─── PERMISSION RESULT ────────────────────────────
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
                }
            }
            NOTIFICATION_PERMISSION_CODE -> {
                Log.d("SmartBudget", "Notification permission result: ${grantResults.firstOrNull()}")
            }
        }
    }
}