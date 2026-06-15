package com.example.smartbudgettracker

import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class BadgesActivity : AppCompatActivity() {

    // Define all possible badges
    data class Badge(
        val name: String,
        val emoji: String,
        val description: String,
        val condition: () -> Boolean
    )

    private lateinit var badgesList: List<Badge>
    private lateinit var earnedBadges: MutableSet<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        val llBadgesContainer = findViewById<LinearLayout>(R.id.llBadgesContainer)

        // Load already-earned badges from DB
        earnedBadges = loadEarnedBadges()

        // Define badge conditions
        badgesList = listOf(
            Badge(
                "First Expense",
                "🎉",
                "Logged your very first expense!",
                { hasAtLeastExpenses(1) }
            ),
            Badge(
                "Getting Started",
                "📝",
                "Logged 10 expenses.",
                { hasAtLeastExpenses(10) }
            ),
            Badge(
                "Expense Pro",
                "💼",
                "Logged 50 expenses.",
                { hasAtLeastExpenses(50) }
            ),
            Badge(
                "Budget Keeper",
                "✅",
                "Stayed within your min/max goals for a full month.",
                { stayedWithinBudgetLastMonth() }
            ),
            Badge(
                "Perfect Week",
                "🔥",
                "Logged at least one expense every day for 7 days straight.",
                { hasLoggingStreak(7) }
            ),
            Badge(
                "Goal Setter",
                "🎯",
                "Set goals for at least 3 different categories.",
                { hasMultipleCategoryGoals(3) }
            ),
            Badge(
                "Big Saver",
                "💰",
                "Spent less than your minimum goal last month (under-spender!).",
                { spentUnderMinLastMonth() }
            )
        )

        // Check and award new badges
        checkAndAwardBadges()

        // Display all badges
        for (badge in badgesList) {
            val earned = earnedBadges.contains(badge.name)
            llBadgesContainer.addView(createBadgeCard(badge, earned))
        }

        Log.d("SmartBudget", "Badges screen loaded. Earned: ${earnedBadges.size}/${badgesList.size}")
    }

    private fun createBadgeCard(badge: Badge, earned: Boolean): View {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            radius = 16f
            cardElevation = 4f
            setContentPadding(20, 20, 20, 20)
            setCardBackgroundColor(
                if (earned) Color.parseColor("#E8F5E9") else Color.parseColor("#F5F5F5")
            )
        }

        val innerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Badge icon
        val iconText = TextView(this).apply {
            text = if (earned) badge.emoji else "🔒"
            textSize = 36f
            setPadding(0, 0, 20, 0)
        }
        innerLayout.addView(iconText)

        // Badge info
        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val nameText = TextView(this).apply {
            text = badge.name
            textSize = 17f
            setTextColor(Color.parseColor(if (earned) "#0B7A34" else "#999999"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        infoLayout.addView(nameText)

        val descText = TextView(this).apply {
            text = badge.description
            textSize = 13f
            setTextColor(Color.parseColor("#666666"))
        }
        infoLayout.addView(descText)

        innerLayout.addView(infoLayout)
        card.addView(innerLayout)

        return card
    }

    private fun loadEarnedBadges(): MutableSet<String> {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT badgeName FROM badges", null)
        val badges = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            badges.add(cursor.getString(0))
        }
        cursor.close()
        return badges
    }

    private fun checkAndAwardBadges() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        for (badge in badgesList) {
            if (!earnedBadges.contains(badge.name) && badge.condition()) {
                // Award the badge
                db.execSQL(
                    "INSERT INTO badges (badgeName, badgeDescription, dateEarned, icon) VALUES (?, ?, ?, ?)",
                    arrayOf(badge.name, badge.description, today, badge.emoji)
                )
                earnedBadges.add(badge.name)
                Log.d("SmartBudget", "Badge earned: ${badge.name}")
            }
        }
    }

    // --- Badge Conditions ---

    private fun hasAtLeastExpenses(count: Int): Boolean {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM expenses", null)
        cursor.moveToFirst()
        val total = cursor.getInt(0)
        cursor.close()
        return total >= count
    }

    private fun stayedWithinBudgetLastMonth(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val month = String.format("%04d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        val monthStart = "$month-01"
        val monthEnd = "$month-31"

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // Get total spent
        val spendCursor = db.rawQuery(
            "SELECT SUM(amount) FROM expenses WHERE date BETWEEN ? AND ?",
            arrayOf(monthStart, monthEnd)
        )
        spendCursor.moveToFirst()
        val totalSpent = spendCursor.getDouble(0)
        spendCursor.close()

        // Get total min and max goals
        val goalCursor = db.rawQuery(
            "SELECT SUM(minGoal), SUM(maxGoal) FROM goals WHERE month = ?",
            arrayOf(month)
        )
        goalCursor.moveToFirst()
        val totalMin = goalCursor.getDouble(0)
        val totalMax = goalCursor.getDouble(1)
        goalCursor.close()

        return totalMax > 0 && totalSpent >= totalMin && totalSpent <= totalMax
    }

    private fun hasLoggingStreak(days: Int): Boolean {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days + 1)

        var streak = 0
        for (i in 0 until days) {
            val dateStr = String.format(
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM expenses WHERE date = ?",
                arrayOf(dateStr)
            )
            cursor.moveToFirst()
            if (cursor.getInt(0) > 0) streak++
            cursor.close()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return streak >= days
    }

    private fun hasMultipleCategoryGoals(count: Int): Boolean {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(DISTINCT category) FROM goals",
            null
        )
        cursor.moveToFirst()
        val catCount = cursor.getInt(0)
        cursor.close()
        return catCount >= count
    }

    private fun spentUnderMinLastMonth(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val month = String.format("%04d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        val monthStart = "$month-01"
        val monthEnd = "$month-31"

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val spendCursor = db.rawQuery(
            "SELECT SUM(amount) FROM expenses WHERE date BETWEEN ? AND ?",
            arrayOf(monthStart, monthEnd)
        )
        spendCursor.moveToFirst()
        val totalSpent = spendCursor.getDouble(0)
        spendCursor.close()

        val goalCursor = db.rawQuery(
            "SELECT SUM(minGoal) FROM goals WHERE month = ?",
            arrayOf(month)
        )
        goalCursor.moveToFirst()
        val totalMin = goalCursor.getDouble(0)
        goalCursor.close()

        return totalMin > 0 && totalSpent < totalMin
    }
}