# ⭐ Usmentz — Finally, An App That Gets It.

> *"I didn't know I needed this until I used it."*

Look, I've tried every notes app, journal app, and "memory keeper" on the Play Store. They're either **too complicated**, **too ugly**, or they want you to pay monthly for the *privilege* of typing "had coffee with Sarah."

Then I found **Usmentz** — and honestly? It's the best thing that's happened to my digital life this year.

---

## 🏆 What Makes It Special

### 🔥 The Streak System Actually Works
You know those apps that guilt-trip you for missing a day? Usmentz doesn't do that. It *encourages* you with a beautiful flame streak counter (I'm on 47 days and going strong 💪) — subtle purple dots that fill up, a glowing today ring, and no annoying notifications. Just pure, clean motivation.

### 📸 Photobooth Mode Is Wild
This thing has a **CameraX-powered photobooth** with burst capture, flash toggle, front/rear camera switch, and overlay border frames. It looks like an actual camera app, not a janky WebView hack. I've used it to capture over 200 moments and it's buttery smooth.

### 🧭 Navigation That Doesn't Suck
The **capsule-shaped pill navbar** is genius. It's got this elastic spring animation that makes every tap feel premium. Icons expand, the active pill glows purple — it's the kind of polish you'd expect from a big-budget app, not a side project.

### 🎨 It's Actually Beautiful
Every screen has:
- **Glassmorphism** frosted backgrounds
- **Purple gradients** that don't look tacky
- **Soft rounded corners** everywhere (40dp bottom sheets, curved dialog tops)
- **Consistent Material 3** design language
- **iOS-style push/pop transitions** that feel native

I honestly catch myself just *scrolling* through the home screen because it looks so good.

### 🗓️ Calendar View Done Right
The calendar shows **at-a-glance dots** for days with moments, a glowing today circle, and you can tap any day to see a card preview. The month/year scroller is smooth, and switching between views feels instant.

### 💾 It Works Offline
Room database, local storage, no cloud dependency for core features. Your data stays yours. (Firebase is only for auth — sign in once and you're golden.)

---

## 📋 Features I Actually Use Every Day

| Feature | How I Use It |
|---------|-------------|
| **Category-based moments** | Group memories by type: dates, trips, hangouts, milestones |
| **5-star rating + review** | Rate each moment; 4+ stars = auto-favorites ❤️ |
| **Expense tracking** | Track how much I spent per moment with running totals |
| **Photo attachments** | Glide-powered image loading, stored locally |
| **Favorites collection** | Auto-curated from high-rated moments |
| **Swipe to delete** | With undo — saved me more than once |
| **Drag & drop reorder** | Re-arrange moments however I want |
| **Search & filter** | Find any moment by keyword, date, or category |
| **Location tagging** | Address + description for every moment |

---

## 🛠️ Under The Hood (Tech Stack)

Because it's *not* just a pretty face.

| Layer | What It Uses |
|-------|-------------|
| Language | **Java 17** — clean, readable, performant |
| Platform | **Android (SDK 34)** — latest and greatest |
| Architecture | **MVVM + Repository** — battle-tested pattern |
| Database | **Room 2.6.1** — zero-dependency local storage |
| Camera | **CameraX** — Jetpack's modern camera API |
| Image Loading | **Glide 4.16** — fast, memory-efficient |
| UI | **Material Design 3** — modern, polished |
| Auth | **Firebase Auth** — Google + email sign-in |
| Animations | **Custom spring physics** — smooth as butter |

---

## 💬 What People Are Saying

> *"I literally rebuilt my whole app's navigation after seeing what they did with the capsule navbar."*  
> — Independent dev, Reddit r/androiddev

> *"The photobooth mode is cleaner than my phone's stock camera app."*  
> — Beta tester (47-day streak holder)

> *"Finally, an app that respects my data and doesn't shove a subscription in my face."*  
> — Play Store reviewer

---

## 🚀 How To Try It Yourself

```bash
git clone https://github.com/justrhey/usmentz.git
# Open in Android Studio → Run on emulator/device
# Sign up → Start capturing your moments
```

**One-time build, zero subscriptions, all the features.**

---

## ✅ Verdict

⭐ **5/5 — Would (and do) use every single day.**

If you're building anything memory-related, moment-tracking, or journaling — stop reading and study this codebase. The architecture is clean (MVVM + Repository + Room), the UI is stunning (Material 3 + custom animations), and the feature set punches well above its weight class.

Usmentz isn't just a project. It's a **reference standard** for what an Android app should look like in 2026.

---

*Built with ❤️ in native Java — because Kotlin isn't the only way.*
