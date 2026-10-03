# 🛡️ StashGuard
*Guarding Your Stash. Building Your Freedom.*

StashGuard is a personal finance app built for people who think ahead. 
Designed with Kotlin Multiplatform, it helps you save intentionally, track goals with purpose, and move confidently toward financial independence — without stress or complexity.

---

https://github.com/user-attachments/assets/573745fb-1ff7-424b-8a81-63d43673763b

---

## ✨ Features

- **Accounts** — create, edit and color-code accounts, mark debt accounts, and see the total balance at a glance
- **Custom order** — long-press an account and drag it to rearrange the list
- **Operations** — record revenue, expenses and transfers between accounts; balances update automatically
- **Categories** — organize operations with categories (a default set is created on first launch)
- **Account details** — last 30 days of revenue, expenses and net change, plus recent activity
- **Offline-first** — all data stays on the device

## 🛠️ Tech Stack

| Category | Technology |
|--------|-----------------------------------------------------------------------|
| **Platform** | Kotlin Multiplatform (Android, iOS) |
| **UI Framework** | Compose Multiplatform + Material 3 |
| **Navigation** | Navigation 3 |
| **State Management** | `StateFlow` + `ViewModel` (MVVM pattern) |
| **Data Storage** | Room (KMP) with bundled SQLite |
| **Dependency Injection** | Koin |
| **Logging** | Kermit |
| **Build** | Gradle 9.4, AGP 9.2, Kotlin 2.4 |

## 📁 Project Structure

```
composeApp/   Shared KMP library — UI, ViewModels, Room database (nearly all code is in commonMain)
androidApp/   Android application — MainActivity, Application class, manifest and launcher icons
iosApp/       Xcode project — SwiftUI entry point that hosts the Compose UI
```

## 🚀 Getting Started

Requirements: JDK 17+, Android Studio (or the Android SDK with API 37), and Xcode for iOS.

```bash
./gradlew :androidApp:installDebug                          # build and install on a device/emulator
./gradlew :composeApp:compileKotlinIosSimulatorArm64        # compile-check the iOS code
```

To run on iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme.

## 🗺️ Roadmap

- Web target (Compose for Web / Wasm)
- Sync: Firebase (optional) or private server
- Authentication: Email/Password + Google Sign-In
- End-to-end encryption
- Savings goals
