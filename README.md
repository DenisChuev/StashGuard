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
- **Operations** — record revenue, expenses and transfers between accounts, and edit them later; balances update automatically
- **Categories** — organize operations with categories (a default set is created on first launch)
- **Account details** — last 30 days of revenue, expenses and net change, plus recent activity
- **Offline-first** — all data stays on the device

## 🛠️ Tech Stack

| Category | Technology |
|--------|-----------------------------------------------------------------------|
| **Platform** | Kotlin Multiplatform (Android, iOS) |
| **UI Framework** | Compose Multiplatform + Material 3 |
| **Navigation** | Navigation 3 |
| **Architecture** | Multi-module Clean Architecture (feature / domain / data layers), MVVM |
| **State Management** | `StateFlow` + `ViewModel` |
| **Data Storage** | Room (KMP) with bundled SQLite |
| **Dependency Injection** | Koin |
| **Logging** | Kermit |
| **Build** | Gradle 9.6, AGP 9.4, Kotlin 2.4 |

## 📁 Project Structure

```
androidApp/             Android application — MainActivity, Application class, manifest and launcher icons
iosApp/                 Xcode project — SwiftUI entry point that hosts the Compose UI
composeApp/             App shell shared by Android and iOS — navigation, Koin wiring, iOS framework
feature/accounts/       Accounts list, add / edit account, account details (screens + ViewModels)
feature/operations/     Operations list, add / edit operation
feature/categories/     Categories list
core/domain/            Models, repository interfaces and use cases (pure Kotlin)
core/data/              Repository implementations, entity <-> model mappers
core/database/          Room database, entities and DAOs
core/ui/                Compose helpers shared by the features
core/common/            Date and currency utilities
build-logic/            Gradle convention plugins (stashguard.kmp.library / .compose / .feature)
```

Dependencies point inwards: `feature:*` → `core:domain` ← `core:data` → `core:database`.
Features never see Room or the data layer; `composeApp` wires everything together with Koin.
Balance changes (adding or editing operations, transfers, deleting accounts) go through use cases in `core:domain`.

## 🚀 Getting Started

Requirements: JDK 17+, Android Studio (or the Android SDK with API 37), and Xcode for iOS.

```bash
./gradlew :androidApp:installDebug                          # build and install on a device/emulator
./gradlew :composeApp:compileKotlinIosSimulatorArm64        # compile-check the iOS code
```

To run on iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme.

## 🗺️ Roadmap

**Phase 1 — first store release (autumn 2026)**

- MVI presentation layer and kotlin-inject for DI
- CI with tests (`commonTest`, Turbine) and code coverage
- Currency per account with exchange rates
- Reports and charts by category and period
- Recurring operations
- Biometric lock and database encryption
- Release in Google Play, App Store, RuStore and Huawei AppGallery

**Later**

- Web target (Compose for Web / Wasm)
- Sync: Firebase (optional) or private server
- Authentication: Email/Password + Google Sign-In
- End-to-end encryption
- Savings goals
