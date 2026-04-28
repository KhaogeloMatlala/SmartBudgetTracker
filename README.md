# SmartBudget Tracker

## Overview

SmartBudget Tracker is an Android application developed using Kotlin in Android Studio.
The purpose of the application is to help users manage their personal finances by tracking expenses, setting monthly spending goals, organising spending categories, and viewing expense summaries over selected periods.

The application uses SQLite for local offline data storage and includes input validation, photo capture for expense entries, category summaries, and date filtering for better financial management.

---

## Features

### User Login

* Users enter username and password to access the application

### Dashboard

* Central screen used to navigate to all app features

### Category Management

* Users can create and save expense categories

### Expense Entries

* Users can record:

  * Date
  * Start time
  * End time
  * Description
  * Category
  * Amount spent
  * Photo evidence (captured using the camera)

### Monthly Spending Goals

* Users can set:

  * Minimum monthly goal
  * Maximum monthly goal

### Expense Viewing

* Users can filter and view expenses between selected start and end dates

### Category Summary

* Users can view total spending per category within a selected date range

### Local Database Storage

* SQLite database stores:

  * Categories
  * Expenses
  * Monthly goals

### Validation and Logging

* Input validation prevents invalid entries
* Logcat logging is used for debugging and monitoring app activity

---

## Technologies Used

* Kotlin
* Android Studio
* XML Layouts
* SQLite (SQLiteOpenHelper)
* Android Camera Intent
* Logcat
* GitHub (for version control)

---

## How to Run the Project

1. Open the project in Android Studio

2. Allow Gradle to sync

3. Build the project using:

   Build → Rebuild Project

4. Run the application using:

   * Android Emulator
     OR
   * Physical Android Device

5. Test all features:

   * Login
   * Dashboard navigation
   * Category creation
   * Expense saving
   * Goal setting
   * Expense filtering
   * Category summary

---

## Author

Khaogelo Matlala

ST10453718

OPSC6311

28 April 2026

---

## References

Android Developers. (2026). Activities and Intents. Retrieved from https://developer.android.com

Android Developers. (2026). SQLiteOpenHelper. Retrieved from https://developer.android.com

Android Developers. (2026). Camera Intents. Retrieved from https://developer.android.com

Kotlin Documentation. (2026). Kotlin for Android Development. Retrieved from https://kotlinlang.org

GeeksforGeeks. (2026). Android SQLite Database Tutorial. Retrieved from https://www.geeksforgeeks.org

TutorialsPoint. (2026). Android SQLite Tutorial. Retrieved from https://www.tutorialspoint.com

---

## Declaration

I confirm that this work is my own original academic work and that all sources used have been properly acknowledged.
