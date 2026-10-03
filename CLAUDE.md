# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

StashGuard is a personal finance app (accounts, operations/transactions, categories) built with Kotlin Multiplatform + Compose Multiplatform. Shared code lives in the `:composeApp` module (a KMP library using the `com.android.kotlin.multiplatform.library` plugin); nearly everything is in `commonMain`. `:androidApp` is a thin Android application module that depends on `:composeApp` and holds only `MainActivity`, `StashGuardApp`, the manifest and launcher resources. AGP 9 no longer allows `com.android.application` in a KMP module, so keep that split. Active targets are Android and iOS (`iosArm64`, `iosSimulatorArm64`). The JS/Wasm targets in `composeApp/build.gradle.kts` and the `:server` module in `settings.gradle.kts` are commented out, so `webMain` and `server/` are not built.

## Commands

```bash
./gradlew :androidApp:assembleDebug                         # build the Android debug APK
./gradlew :androidApp:installDebug                          # install on a connected device or emulator
./gradlew :composeApp:compileKotlinIosSimulatorArm64        # compile-check the iOS code
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64   # build the iOS framework
```

Run the iOS app by opening `iosApp/iosApp.xcodeproj` in Xcode. There is no test source set (`commonTest` is commented out in the build file) and no lint or format config.

Dependency versions live in `gradle/libs.versions.toml`.

## Architecture

**Layers.** Each screen is a Compose screen plus a ViewModel (MVVM) that exposes `StateFlow`s. ViewModels inject the Room DAOs directly; there is no repository layer. A ViewModel maps entities (`data/local/*Entity`) to domain models (`model/*`) using the `toX()` / `toXEntity()` extension functions in the model files. Example: `Account.color` is a Compose `Color`, while `AccountEntity.color` is an ARGB `Int`, and timestamps are stored as epoch seconds.

**Feature layout.** Code is organized as `screens/<feature>/<sub_screen>/{XScreen,XViewModel}.kt`, for example `screens/accounts/edit_account/`.

**Dependency injection (Koin)** is set up in `di/Koin.kt`:
- `databaseModule` provides the DAOs.
- `viewModelModule` registers every ViewModel. A new ViewModel must be registered here.
- `expect fun platformModule()` provides the platform's `AppDatabase` (see `di/KoinPlatformModule.{android,ios}.kt`).
- ViewModels that take parameters (such as `accountId`) are declared as `viewModel { (id: String) -> ... }`, and screens get them with `koinViewModel { parametersOf(id) }`.
- `initKoin` is called once at app startup: from `StashGuardApp.onCreate()` (in `:androidApp`) on Android, and from `iOSApp.init()` in Swift on iOS (through `setupKoin()` in `MainViewController.kt`).

**Navigation** uses Navigation 3 (`org.jetbrains.androidx.navigation3`):
- Routes are `@Serializable` objects or data classes implementing `NavKey` in `navigation/Routes.kt`. A new route must also be registered in the polymorphic `SerializersModule` in `navigation/NavigationState.kt`; iOS has no reflection, so an unregistered route crashes when the back stack is saved.
- Screens are registered in the `entryProvider { entry<Route> { ... } }` block in `navigation/AppNavigation.kt`, rendered by a single `NavDisplay`.
- `NavigationState` keeps one back stack per bottom-bar tab (`BottomNavigationItem` in `navigation/BottomNavigationItems.kt`). `navigate(key)` switches tab when `key` is a tab and otherwise pushes onto the current tab's stack; `goBack()` pops, or returns to the first tab from another tab's root. The bottom bar shows only at a tab root.
- Each stack is decorated with `rememberViewModelStoreNavEntryDecorator()`, which gives every entry its own `ViewModelStore`. Without it, `koinViewModel { parametersOf(id) }` would reuse one ViewModel for every screen of the same type.
- Screens never touch the navigation state. They receive navigation lambdas (`onNavigateBack`, `onNavigateToX`).

**Persistence** uses Room KMP with the bundled SQLite driver:
- The database is defined in `data/local/AppDatabase.kt`. The platform builders (`DatabaseBuilder.{android,ios}.kt`) supply the database file path.
- A schema change means bumping `version` in `@Database` and adding a migration (an `AutoMigration` in `autoMigrations` is enough for simple changes such as a new column with `@ColumnInfo(defaultValue = ...)`). Exported schemas in `composeApp/schemas/` are what auto-migrations diff against, so commit them. `fallbackToDestructiveMigration(true)` is still on, so a missing migration silently wipes the user's data.
- The Room compiler runs through KSP, configured per target in the `dependencies {}` block of `composeApp/build.gradle.kts`. A new native target also needs a `kspXxx` entry there.

**Account balances** are stored on `AccountEntity` and are not computed from operations. When an operation is added or edited, the ViewModel (for example `AddOperationViewModel`) changes the related account's balance itself, including for transfers between accounts. Any code that creates, edits or deletes operations has to keep balances consistent.

**Utilities.**
- `util/CurrencyUtils.kt` has `toBalanceString()` and `toBalanceDouble()` for formatting and parsing balance input.
- `util/DateUtils.kt` wraps `kotlin.time.Instant`. This API needs `@OptIn(ExperimentalTime::class)`, and UUID ids need `ExperimentalUuidApi`; both are opted in per file.
- Logging uses Kermit: `Logger.withTag("...")`.
- Compose resources are generated into the `dc.stashguard.resources` package.
