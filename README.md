# Money Trees Budget Tracker App

*A Gamified Expense Management Solution for Android*

---

##  Project Overview

**Money Trees** is an intuitive, gamified Android budgeting app built in Kotlin. Designed for modern personal finance management, it helps users control spending, achieve savings goals, and engage in money habits with fun and accountability.

###  Target Audience

Individuals who want to take control of their personal finances through intuitive tools and motivational gamification. Ideal for students, young professionals, families, and digital nomads.

### Key Objectives

* Simplify expense logging with a clean, fast UI.
* Motivate consistent usage with achievements and interactive progress tracking.
* Deliver actionable financial insights via rich data visualization and alerts.
* Foster shared accountability through shared wallets and leaderboards.

---

## What the App Does

The **Money Trees Budget App** empowers users to:

* Track daily expenses and income in categorized formats.
* Set personalized monthly budgets and category-specific limits.
* View real-time analytics on spending trends and savings.
* Stay motivated with mini-games, leaderboards, and achievement systems.
* Manage shared budgets with friends, family, or roommates using shared wallets.
* Track finances across multiple currencies (perfect for international travelers).
* Receive alerts for overspending, upcoming bills, and budget milestones.
* Sync financial data between mobile and desktop with simulated bank support.

---

## Core Features

### Gamification

* **Mini-Games** like *Budget Bingo* & *Saving Quest* to reward smart habits.
* **Points & Rewards** for good budgeting behavior.
* **Leaderboards** to compete with friends and global users.
* **Achievements** (e.g., “Budget Boss”, “Savings Streak”, “Spending Sniper”).

### Budgeting & Financial Goals

* Set monthly budget and category-specific goals.
* View dashboards with spending vs. budget visuals.
* Get alerts when approaching or exceeding limits.

### Analytics & Insights

* Interactive charts and graphs (MPAndroidChart).
* Daily trends, spending category breakdowns, and savings progress.
* Weekly and monthly report generation.

### Multi-Currency 

* Track and convert expenses in multiple currencies.

### Authentication & Security

* User login and account management.
* End-to-end encryption and secure local storage via Room.
* Session expiration for added safety.

---

## Technical Highlights

* **Kotlin** as primary development language.
* **Room Database** for persistent local data storage.
* **Glide** for smooth receipt image uploads.
* **MPAndroidChart** for dynamic financial graphs.
* **MVVM Architecture** to ensure scalability and maintainability.
* **Wi-Fi Sync Support** between desktop and mobile for simulated PC-based management.

---

## Database Structure (Room ORM)

| Entity        | Fields                                                                                          | Description                             |
| ------------- | ----------------------------------------------------------------------------------------------- | --------------------------------------- |
| `User`        | `userId`, `username`, `password`                                                                | Stores user credentials.                |
| `Category`    | `categoryId`, `name`                                                                            | Custom expense categories.              |
| `Expense`     | `expenseId`, `amount`, `date`, `startTime`, `endTime`, `description`, `categoryId`, `photoPath` | Expense entries with optional receipts. |
| `BudgetGoal`  | `budgetId`, `monthlyLimit`, `categoryLimit`                                                     | Monthly and category budgets.           |
| `Achievement` | `achievementId`, `name`, `criteria`                                                             | Badges based on app usage and success.  |

---

## Installation

### Requirements

* Android Studio (latest version recommended)
* Android device or emulator (API 26+)

### Steps

```bash
git clone https://github.com/IIEMSA/opsc6311-poe-part-2-group1-Lwandle-Chauke
cd MoneyTrees
```

1. Open the project in Android Studio.
2. Allow Gradle to sync and download dependencies.
3. Run the app on your emulator or connected Android device.

### Dependencies

```gradle
dependencies {
    implementation "androidx.room:room-runtime:2.5.0"
    kapt "androidx.room:room-compiler:2.5.0"
    implementation 'com.github.bumptech.glide:glide:4.15.1'
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
}
```

---

## Development Process

The development followed a **Research → Plan → Design → Build → Evaluate** lifecycle.

### Research

* Reviewed apps like Mint, YNAB, and Goodbudget.
* Found common complaints: steep learning curves, lack of visual engagement.

### Planning

* Used MoSCoW to prioritize: Must-Have (tracking, goals, shared wallets), Should-Have (analytics, mini-games), etc.
* Structured architecture with MVVM, Room, and Repository patterns.

### Design

* Wireframes focused on clarity, ease of use, and visual feedback.
* Created playful but professional UI aligned with a natural theme (trees, coins, growth).

### Build

* Developed in agile sprints with defined deliverables.
* Emphasis on modular components, scalability, and UI/UX polish.

### Evaluate

* Conducted usability testing among peers.
* Gathered feedback and iterated on gamification, graphs, and navigation.

---

## Future Enhancements

* Real-time bank API integration (Plaid or SaltEdge).
* AI-based financial coaching.
* Dark mode and accessibility enhancements.
* Expand mini-games and integrate with Google Play Games Services.

---

## License

This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.

---
**Contact**: \ST10380788@IMCONNECT.EDU.ZA/ST10361620@IMCONNECT.EDU.ZA/ST10369736@IMCONNECT.EDU.ZA | [GitHub Profile](https://github.com/IIEMSA/opsc6311-poe-part-2-group1-Lwandle-Chauke)
**Submission**: This project is part of the Portfolio of Evidence (POE) for OPSC6311.
