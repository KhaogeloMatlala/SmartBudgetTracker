package com.example.smartbudgettracker

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/*
 * DatabaseHelper
 *
 * This class manages the local SQLite database for the SmartBudget Tracker app.
 * It creates the database tables and handles upgrades when the database version changes.
 */

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "SmartBudgetDB", null, 1) {

    /*
     * onCreate()
     *
     * Runs only once when the database is first created.
     * This method creates:
     * 1. Categories table
     * 2. Expenses table
     * 3. Goals table
     */

    override fun onCreate(db: SQLiteDatabase) {


        db.execSQL("""
    CREATE TABLE users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT NOT NULL,
        password TEXT NOT NULL
    )
""")
        // Table for storing expense categories
        db.execSQL("""
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """)

        // Table for storing expense entries
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

        // Table for storing monthly spending goals
        db.execSQL("""
            CREATE TABLE goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                minGoal REAL,
                maxGoal REAL
            )
        """)
    }

    /*
     * onUpgrade()
     *
     * Runs when the database version changes.
     * Old tables are removed and recreated.
     */

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS expenses")
        db.execSQL("DROP TABLE IF EXISTS goals")

        // Recreate updated tables
        onCreate(db)
    }
}