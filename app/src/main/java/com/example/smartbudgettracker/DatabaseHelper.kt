package com.example.smartbudgettracker

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/*
 * DatabaseHelper
 *
 * This class manages the local SQLite database for the SmartBudget Tracker app.
 * It creates the database tables and handles upgrades when the database version changes.
 *
 * Tables:
 * - users: stores user credentials
 * - categories: expense categories
 * - expenses: logged expenses
 * - goals: monthly min/max spending goals per category
 * - badges: earned gamification badges
 */

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "SmartBudgetDB", null, 2) {  // version bumped to 2

    override fun onCreate(db: SQLiteDatabase) {
        // Users table
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                password TEXT NOT NULL
            )
        """)

        // Categories table
        db.execSQL("""
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """)

        // Expenses table
        db.execSQL("""
            CREATE TABLE expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                startTime TEXT,
                endTime TEXT,
                description TEXT,
                category TEXT,
                amount REAL,
                photo TEXT
            )
        """)

        // Goals table — now supports per-category goals with a month field
        db.execSQL("""
            CREATE TABLE goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                month TEXT NOT NULL,
                minGoal REAL,
                maxGoal REAL
            )
        """)

        // Badges table — tracks earned badges
        db.execSQL("""
            CREATE TABLE badges (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                badgeName TEXT NOT NULL,
                badgeDescription TEXT,
                dateEarned TEXT,
                icon TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop all tables and recreate
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS expenses")
        db.execSQL("DROP TABLE IF EXISTS goals")
        db.execSQL("DROP TABLE IF EXISTS badges")
        onCreate(db)
    }
}