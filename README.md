# Usmentz

A personal moments tracker for Android. Helps you organize memories, rate experiences, and keep tabs on what matters.

---

## About

Usmentz lets you log moments by category, attach photos, rate them, add expenses, and review them later. Built with Java on Android.

---

## Features

- Category-based moment grouping
- 5-star rating with text reviews
- Expense tracking per moment
- Photo attachments via camera or gallery
- Auto-collected favorites (4+ star moments)
- Calendar overview with day dots
- Swipe-to-delete with undo
- Drag-and-drop reorder
- Search and filter by keyword or date
- Location tagging with address

---

## Tech Stack

| Layer | |
|-------|-|
| Language | Java 17 |
| Platform | Android SDK 34 |
| Architecture | MVVM + Repository |
| Database | Room 2.6.1 |
| Camera | CameraX |
| Image Loading | Glide 4.16 |
| UI | Material Design 3 |
| Auth | Firebase Auth |

---

## Getting Started

```bash
git clone https://github.com/justrhey/usmentz.git
```

Open in Android Studio, sync Gradle, and run on a device or emulator.

Firebase Authentication must be enabled in your Firebase project.

---

## Project Structure

```
usmentz/
app/src/main/java/com/example/usmentz/
  ├── Activities/
  ├── adapter/
  ├── database/
  ├── dao/
  ├── repo/
  └── viewmodel/
app/src/main/res/
  ├── layout/
  ├── drawable/
  ├── values/
  └── anim/
```

---

## License

Personal use.
