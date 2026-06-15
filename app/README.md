# SmartBudget SA

## 📱 App Purpose
SmartBudget SA is a personal finance management Android app built in Kotlin. It helps users track daily expenses, set monthly spending goals per category, view spending graphs, and stay motivated through gamification badges. The app is designed for South African users who want a simple, offline-capable budget tracker.

## 🎨 Design Considerations
- **Colour scheme:** Green and white to represent financial growth and clarity
- **Typography:** Bold, modern fonts for readability
- **Navigation:** Simple dashboard with clear button-based navigation
- **Offline-first:** All data stored locally in SQLite — no internet required
- **User feedback:** Toast messages, notifications, and visual health indicators

## 🚀 Features

### Core Features
- User registration and login
- Create custom expense categories
- Log expenses with date, time, description, category, amount
- Set monthly min/max spending goals per category
- View all expenses in a modern card list
- Category spending bar graph with goal lines (user-selectable date range)
- Budget health visual showing spending vs goals (ring progress + per-category bars)
- Gamification: Earn badges for meeting budget goals and consistent logging
- Dashboard with financial progress summary card

### Custom Features (Own Features)
1. **Receipt Photo Attachment:** Users can take a photo with the camera or choose from gallery and attach it to an expense entry. Photos are saved to local storage.
2. **Spending Alerts / Notifications:** After saving an expense, the app checks the monthly category budget. A notification is sent if spending reaches 80% (warning) or exceeds 100% (over budget) of the max goal.

## 🛠️ Tech Stack
- **Language:** Kotlin
- **Database:** SQLite (via `SQLiteOpenHelper`)
- **Charts:** MPAndroidChart
- **UI:** XML layouts with Material Design components (CardView)
- **Notifications:** Android NotificationManager with NotificationChannel
- **CI/CD:** GitHub Actions for automated APK builds

## 🔧 GitHub Actions
This project uses GitHub Actions to automatically build the APK on every push to main/master.

**Workflow file:** `.github/workflows/build.yml`

**What it does:**
- Checks out the code
- Sets up JDK 17
- Runs `./gradlew assembleDebug`
- Uploads the debug APK as an artifact

## 🧪 Testing
Automated build testing is performed via GitHub Actions. The main functionality tested includes:
- Successful Gradle build
- APK generation

## 🔗 Links
- **GitHub Repository:https://github.com/KhaogeloMatlala/SmartBudgetTracker.git
- **APK Download:https://github.com/KhaogeloMatlala/SmartBudgetTracker/releases/tag/v1.0

## 📂 Project Structure
app/src/main/java/com/example/smartbudgettracker/
├── MainActivity.kt # Login screen

├── RegisterActivity.kt # Registration screen

├── DashboardActivity.kt # Main dashboard with progress card

├── CategoryActivity.kt # Create and view categories

├── AddExpenseActivity.kt # Add expense with photo + alerts

├── ExpenseListActivity.kt # View all expenses

├── GoalActivity.kt # Set monthly goals per category

├── CategorySummaryActivity.kt # Spending graph with goal lines

├── BudgetHealthActivity.kt # Budget health visual

├── BadgesActivity.kt # Gamification badges

└── DatabaseHelper.kt # SQLite database manager

## 👤 Author
Khaogelo Matlala

## 📄 License
This project is submitted as part of an academic assignment.