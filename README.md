# Money Trees Budget Tracker App

*A Gamified Expense Management Solution for Android*

---

## Project Overview

**Personal Budget Tracker** is an Android application designed to simplify financial management by enabling users to track expenses, set savings goals, and engage with their budgeting through gamification elements. Built with Kotlin and leveraging modern Android development practices, the app ensures a user-friendly experience while maintaining robust functionality.

**Target Audience**: Individuals aiming to manage personal finances, monitor spending habits, and achieve savings goals in an engaging and stress-free manner.

**Key Objectives**:

* Facilitate easy logging of expenses with categories, photos, and date/time tracking.
* Encourage consistent usage through achievements, badges, and progress visualizations.
* Provide actionable insights via graphs, budgets, and category-wise spending analysis.

---
##Screenshots
![image](https://github.com/user-attachments/assets/71e3aee4-d378-4f0f-a62a-1270a94e7997)
![image](https://github.com/user-attachments/assets/4c4db86f-2fbb-4e06-be2a-f468832389c9)
![image](https://github.com/user-attachments/assets/f0dd214b-5eb7-499a-908b-4387e048eb69)
![image](https://github.com/user-attachments/assets/dc10bdf0-840f-4aca-ae51-9efb707d4520)
![image](https://github.com/user-attachments/assets/8a844bba-d3ca-4d38-aa3e-a3e5a55c3182)
![image](https://github.com/user-attachments/assets/a25816f9-d3de-4b61-ac6c-cc9bd89f864b)
![image](https://github.com/user-attachments/assets/f7174a2e-96e1-4f4a-9996-f183bebd726b)
![image](https://github.com/user-attachments/assets/60aa112b-3a03-488d-beac-b42b1b7238ea)
![image](https://github.com/user-attachments/assets/0b9691b5-0db7-41a5-b010-e3f0a4c4b618)
![image](https://github.com/user-attachments/assets/824b8901-2c89-4e5a-8b88-2ff46e991dfd)











## Features

### User Authentication

* Secure registration and login using a username and password.
* Credentials stored locally using Room Database for offline access.

### Expense Management

* Log expenses with details including amount, date, time range, description, category, and optional receipt photos.
* View and filter expenses by date range or category.

### Category Customisation

* Create and delete custom expense categories (e.g., "Groceries," "Entertainment").

### Budget Goals

* Set monthly total budget goals and category-specific limits.
* Monitor progress through a dashboard with visual indicators for overspending.

### Data Visualisation

* Display daily spending trends using interactive graphs powered by MPAndroidChart.
* Show category-wise totals in horizontal RecyclerView bars.

### Gamification Elements (coming soon)

* Earn badges for consistent logging (e.g., "7-Day Streak").
* Unlock achievements for meeting budget goals (e.g., "Frugal Champion").
* Experience progress animations and celebratory feedback upon reaching milestones.

### Technical Highlights

* **Local Database**: Implemented using Room for offline data persistence.
* **Image Handling**: Utilizes Glide library for efficient photo attachment management.
* **Input Validation**: Robust error handling to prevent crashes on invalid inputs.

---

## Installation

### Prerequisites

* Android Studio (latest version)
* Android device or emulator running API level 26 or higher

### Steps

1. Clone the repository:

   ```bash
   https://github.com/IIEMSA/opsc6311-poe-part-2-group1-Lwandle-Chauke
   ```

([Android Developers][1])
2\. Open the project in Android Studio.
3\. Sync Gradle dependencies.
4\. Build and run the application on your device or emulator.

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

## Database Structure

The application employs Room Database for local data storage, ensuring efficient and structured data management.

### Key Entities

| Entity        | Fields                                                                                          | Description                             |   |
| ------------- | ----------------------------------------------------------------------------------------------- | --------------------------------------- | - |
| `User`        | `userId`, `username`, `password`                                                                | Stores user credentials.                |   |
| `Category`    | `categoryId`, `name`                                                                            | Custom expense categories.              |   |
| `Expense`     | `expenseId`, `amount`, `date`, `startTime`, `endTime`, `description`, `categoryId`, `photoPath` | Expense entries with optional receipts. |   |
| `BudgetGoal`  | `budgetId`, `monthlyLimit`, `categoryLimit`                                                     | Monthly and category budgets.           |   |
| `Achievement` | `achievementId`, `name`, `criteria`                                                             | Unlocked badges and rewards.            |   |

---

## Development Process

The development followed the **Research → Plan → Design → Build → Evaluate** cycle:

### 1. Research

* Analyzed competitor apps for user experience patterns.
* Identified user pain points such as tedious logging and lack of motivation.

### 2. Plan

* Prioritised features using the MoSCoW method.
* Defined the technology stack: RoomDB for data management, Glide for image handling, and MPAndroidChart for data visualisation.

### 3. Design

* Created wireframes focusing on intuitive navigation and gamified feedback mechanisms.
* Emphasized user-friendly interfaces with clear visual cues.

### 4. Build

* Implemented features in agile sprints with weekly milestones.
* Adopted a modular architecture using ViewModel, Repository, and DAO patterns.

### 5. Evaluate

* Conducted user testing to gather feedback, leading to simplified photo attachment processes.
* Optimized performance, particularly in RecyclerView handling for large datasets.([Tuts+ Code][2])

---

## Testing

### Unit Tests

* **ExpenseDao Tests**: Ensured CRUD operations function correctly.
* **BudgetGoal Validation**: Verified logic for setting and enforcing budget limits.([Stack Overflow][3])

Example:

```kotlin
@Test
fun testExpenseInsertion() {
    val expense = Expense(amount = 50.0, categoryId = 1)
    database.expenseDao().insert(expense)
    val expenses = database.expenseDao().getAll()
    assertEquals(1, expenses.size)
}
```

## Demonstration Video

A comprehensive [video walkthrough]https://www.youtube.com/  showcases:

1. User registration and login process.
2. Creating expenses with photo attachments.
3. Setting up budget goals and viewing dashboard visualizations.
4. Unlocking achievements through consistent logging.

**Video Guidelines**:

* Duration: 5 minutes with voice-over explanations.
* Resolution: Compressed to 1080p, under 100MB for efficient uploading.

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Acknowledgements

* Icons provided by [Material Design](https://material.io/resources/icons/).
* Graph visualizations powered by [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart).
* Guidance on RoomDB implementation from [Android Developers](https://developer.android.com/training/data-storage/room).([GitHub][4], [Android Developers][1])

---

**Contact**: \ST10380788@IMCONNECT.EDU.ZA/ST10361620@IMCONNECT.EDU.ZA/ST10369736@IMCONNECT.EDU.ZA | [GitHub Profile](https://github.com/IIEMSA/opsc6311-poe-part-2-group1-Lwandle-Chauke)
**Submission**: This project is part of the Portfolio of Evidence (POE) for OPSC6311.

---

Feel free to customize this `README.md` further to align with your specific project details and requirements.

[1]: https://developer.android.com/training/data-storage/room/?utm_source=chatgpt.com "Save data in a local database using Room - Android Developers"
[2]: https://code.tutsplus.com/add-charts-to-your-android-app-using-mpandroidchart--cms-23335t?utm_source=chatgpt.com "Add Charts to Your Android App Using MPAndroidChart"
[3]: https://stackoverflow.com/questions/49799757/implementing-the-room-database-android?utm_source=chatgpt.com "Implementing the Room Database (Android) - Stack Overflow"
[4]: https://github.com/PhilJay/MPAndroidChart?utm_source=chatgpt.com "GitHub - PhilJay/MPAndroidChart: A powerful Android chart view ..."
