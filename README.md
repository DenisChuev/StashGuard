# 🛡️ StashGuard
[![CI](https://github.com/DenisChuev/StashGuard/actions/workflows/ci.yml/badge.svg)](https://github.com/DenisChuev/StashGuard/actions/workflows/ci.yml)

*Guarding Your Stash. Building Your Freedom.*

StashGuard is a personal finance app built for people who think ahead. 
Designed with Kotlin Multiplatform, it helps you save intentionally, track goals with purpose, and move confidently toward financial independence — without stress or complexity.


---

## ✨ Features

- **Accounts** — create, edit and color-code accounts, and see the total balance at a glance
- **Custom order** — long-press an account and drag it to rearrange the list
- **Operations** — record revenue, expenses and transfers between accounts, then edit or delete them; balances update automatically
- **Categories** — a default set is created on first launch; rename them, change their type, color and icon, or add your own. A category that operations use can't be deleted
- **Account details** — the account's full history grouped by day, each day with its net change, and quick buttons to add revenue, expenses or a transfer
- **Light and dark theme** — Material 3, following the system setting
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
| **Testing** | `kotlin.test`, `kotlinx-coroutines-test`, Turbine; in-memory fakes and in-memory Room |
| **CI** | GitHub Actions: Android build + JVM tests on Ubuntu, iOS compile + simulator tests on macOS |
| **Build** | Gradle 9.6, AGP 9.4, Kotlin 2.4 |

## 📁 Project Structure

```
androidApp/             Android application — MainActivity, Application class, manifest and launcher icons
iosApp/                 Xcode project — SwiftUI entry point that hosts the Compose UI
composeApp/             App shell shared by Android and iOS — navigation, Koin wiring, iOS framework
feature/accounts/       Accounts list, add / edit account, account details (screens + ViewModels)
feature/operations/     Operations list, add / edit operation
feature/categories/     Categories list, add / edit category
core/domain/            Models, repository interfaces and use cases (pure Kotlin)
core/data/              Repository implementations, entity <-> model mappers
core/database/          Room database, entities and DAOs
core/ui/                Compose helpers shared by the features
core/common/            Date and currency utilities
core/testing/           Test doubles shared by the tests (fake repositories, test data)
build-logic/            Gradle convention plugins (stashguard.kmp.library / .compose / .feature)
.github/workflows/      CI workflow
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

## 🧪 Testing

```bash
./gradlew testAndroidHostTest       # all tests on the JVM
./gradlew iosSimulatorArm64Test     # all tests on the iOS simulator (needs Xcode)
```

- **Use cases** (`core/domain/src/commonTest`) run against in-memory fake repositories (shared from `core:testing`) and check the resulting balances and stored operations, including both sides of transfers. They run on both the JVM and iOS.
- **ViewModels** (`feature/*/src/commonTest`) use the same fakes with the real use cases: input validation, saving, deleting, loading, and rules such as a category's locked type. They run on both the JVM and iOS.
- **DAOs and transactions** (`core/database` and `core/data`, in `iosTest`) run against a real in-memory Room database, including a check that a failed transaction rolls back all its writes. They run on the iOS simulator only, because Room on the Android JVM needs a `Context` and the bundled SQLite driver has no native library for the host.
- **Dependency graph** (`composeApp/src/androidHostTest`): Koin's `verify()` checks that every use case and ViewModel dependency is registered, so a missing one fails CI instead of crashing a screen.
- Every push to `main` and every pull request runs all of these in CI.

## 🗺️ Roadmap

**Phase 1 — first store release (December 2026)**

- ✅ CI with tests (`commonTest`, Turbine) for use cases, ViewModels, DAOs, transactions and the Koin graph
- ✅ Editable categories
- Code coverage (Kover), static analysis (detekt), signed release builds from CI
- MVI presentation layer
- Currency per account with exchange rates
- Design system (colors, typography, shared components) and a light / dark / system theme setting
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
