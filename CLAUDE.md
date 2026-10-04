# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

StashGuard is a personal finance app (accounts, operations/transactions, categories) built with Kotlin Multiplatform + Compose Multiplatform, organized as a multi-module Clean Architecture project. Active targets are Android and iOS (`iosArm64`, `iosSimulatorArm64`).

Modules (all KMP libraries using `com.android.kotlin.multiplatform.library`, code in `commonMain` unless noted):

| Module | Package | Contents |
|---|---|---|
| `:androidApp` | `dc.stashguard` | Thin Android application: `MainActivity`, `StashGuardApp`, manifest, launcher resources. AGP 9 does not allow `com.android.application` in a KMP module, so keep this split. |
| `:composeApp` | `dc.stashguard` | App shell: `App()`, navigation (routes, `NavigationState`, `AppNavigation`), Koin wiring (`initKoin`), iOS `ComposeApp` framework and `MainViewController`. |
| `:feature:accounts` / `:feature:operations` / `:feature:categories` | `dc.stashguard.feature.<name>` | Presentation layer: screens, ViewModels and a Koin module (`accountsModule`, ...). |
| `:core:domain` | `dc.stashguard.core.domain` | `model/`, `repository/` interfaces and `usecase/`. Pure Kotlin: no Compose, Room or Koin. |
| `:core:data` | `dc.stashguard.core.data` | Repository implementations (`internal`), entity <-> model mappers, `dataModule`. |
| `:core:database` | `dc.stashguard.core.database` | Room `AppDatabase`, entities, DAOs, platform builders, `databaseModule`. |
| `:core:ui` | `dc.stashguard.core.ui` | Compose helpers shared by features (e.g. `Account.color` / `Category.color`). |
| `:core:common` | `dc.stashguard.core.common` | `DateUtils`, `CurrencyUtils`. |

Dependency rule: `feature:*` depends on `core:domain`, `core:ui` and `core:common` only, never on `core:data` or `core:database`. `core:data` depends on `core:domain` and `core:database`. Only `:composeApp` sees every module.

The JS/Wasm targets in `composeApp/build.gradle.kts` and the `:server` module in `settings.gradle.kts` are commented out, so `webMain` and `server/` are not built.

## Commands

```bash
./gradlew :androidApp:assembleDebug                         # build the Android debug APK
./gradlew :androidApp:installDebug                          # install on a connected device or emulator
./gradlew :composeApp:compileKotlinIosSimulatorArm64        # compile-check the iOS code
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64   # build the iOS framework
```

Run the iOS app by opening `iosApp/iosApp.xcodeproj` in Xcode. There is no test source set (`commonTest` is commented out in the build file) and no lint or format config.

Dependency versions live in `gradle/libs.versions.toml`.

**Build logic.** Shared Gradle setup lives in the included build `build-logic/` as convention plugins:
- `stashguard.kmp.library` applies KMP plus the Android KMP library plugin, Android and iOS targets, JVM 11, and derives the Android namespace from the project path (`:core:domain` -> `dc.stashguard.core.domain`).
- `stashguard.kmp.compose` adds Compose Multiplatform and the base Compose dependencies.
- `stashguard.kmp.feature` adds the dependencies every feature needs (core modules, lifecycle, Koin, Kermit, ...).

A new module needs an `include(...)` in `settings.gradle.kts`, a `build.gradle.kts` applying one of these plugins, and (for features) a dependency plus its Koin module in `:composeApp`. The convention plugins use `compileOnly` Gradle plugin dependencies; the plugins are put on the classpath by the root `build.gradle.kts` (`apply false`), so a plugin that a convention applies by id must be declared there too.

## Architecture

**Layers.** Each screen is a Compose screen plus a ViewModel (MVVM) that exposes `StateFlow`s. ViewModels depend on domain repository interfaces (`AccountRepository`, `OperationRepository`, `CategoryRepository`) for plain reads and writes, and on use cases for multi-step business logic (`AddOperationUseCase`, `UpdateOperationUseCase`, `DeleteAccountUseCase`, `AddAccountUseCase`, `CalculateAccountStatisticsUseCase`, `InitializeDefaultCategoriesUseCase`). ViewModels never touch DAOs or entities.

**Models.** Domain models (`core/domain/model`) are framework-free: colors are ARGB `Int`s (`colorArgb`), and `:core:ui` turns them into Compose colors with the `Account.color` / `Category.color` extensions. Entities (`core/database/entity`) store enum types as their `name` strings. The mappers in `core/data/mapper` convert between the two. `Account.createdAt` is stored as epoch seconds and `Operation.createdAt` as epoch milliseconds.

**Feature layout.** Code is organized as `feature/<feature>/src/commonMain/kotlin/dc/stashguard/feature/<feature>/<sub_screen>/{XScreen,XViewModel}.kt`, for example `feature/accounts/.../edit_account/`.

**Dependency injection (Koin).**
- `databaseModule` (`:core:database`) provides `AppDatabase` (through `internal expect fun databasePlatformModule()`, see `DatabasePlatformModule.{android,ios}.kt`) and the DAOs.
- `dataModule` (`:core:data`) binds the repository interfaces to their implementations.
- `domainModule` (in `:composeApp`'s `di/Koin.kt`) registers the use cases as factories, which keeps `:core:domain` free of Koin. A new use case must be registered there.
- Each feature has its own module (`accountsModule`, `operationsModule`, `categoriesModule`) registering its ViewModels. A new ViewModel must be registered in its feature's module.
- ViewModels that take parameters (such as `accountId`) are declared as `viewModel { (id: String) -> ... }`, and screens get them with `koinViewModel { parametersOf(id) }`.
- `initKoin` (in `:composeApp`) is called once at app startup: from `StashGuardApp.onCreate()` (in `:androidApp`) on Android, and from `iOSApp.init()` in Swift on iOS (through `setupKoin()` in `MainViewController.kt`).

**Navigation** uses Navigation 3 (`org.jetbrains.androidx.navigation3`) and lives in `:composeApp`:
- Routes are `@Serializable` objects or data classes implementing `NavKey` in `navigation/Routes.kt`. A new route must also be registered in the polymorphic `SerializersModule` in `navigation/NavigationState.kt`; iOS has no reflection, so an unregistered route crashes when the back stack is saved.
- Screens are registered in the `entryProvider { entry<Route> { ... } }` block in `navigation/AppNavigation.kt`, rendered by a single `NavDisplay`.
- `NavigationState` keeps one back stack per bottom-bar tab (`BottomNavigationItem` in `navigation/BottomNavigationItems.kt`). `navigate(key)` switches tab when `key` is a tab and otherwise pushes onto the current tab's stack; `goBack()` pops, or returns to the first tab from another tab's root. The bottom bar shows only at a tab root.
- Each stack is decorated with `rememberViewModelStoreNavEntryDecorator()`, which gives every entry its own `ViewModelStore`. Without it, `koinViewModel { parametersOf(id) }` would reuse one ViewModel for every screen of the same type.
- Feature screens never touch the navigation state or routes. They receive navigation lambdas (`onNavigateBack`, `onNavigateToX`).

**Persistence** uses Room KMP with the bundled SQLite driver, all in `:core:database`:
- The database is defined in `AppDatabase.kt`. The platform builders (`DatabaseBuilder.{android,ios}.kt`) supply the database file path.
- A schema change means bumping `version` in `@Database` and adding a migration (an `AutoMigration` in `autoMigrations` is enough for simple changes such as a new column with `@ColumnInfo(defaultValue = ...)`). Exported schemas in `core/database/schemas/` are what auto-migrations diff against, so commit them. The schema folder is named after `AppDatabase`'s fully qualified name, so moving that class means renaming the folder. `fallbackToDestructiveMigration(true)` is still on, so a missing migration silently wipes the user's data.
- The Room compiler runs through KSP, configured per target in the `dependencies {}` block of `core/database/build.gradle.kts`. A new native target also needs a `kspXxx` entry there.

**Account balances** are stored on `AccountEntity` and are not computed from operations. `AddOperationUseCase` and `UpdateOperationUseCase` adjust the related accounts' balances (`AccountRepository.adjustBalance`), including both sides of a transfer. Any code that creates, edits or deletes operations has to keep balances consistent, and belongs in a use case.

**Utilities.**
- `core/common/CurrencyUtils.kt` has `toBalanceString()` and `toBalanceDouble()` for formatting and parsing balance input.
- `core/common/DateUtils.kt` wraps `kotlin.time.Instant`. This API needs `@OptIn(ExperimentalTime::class)`, and UUID ids need `ExperimentalUuidApi`; both are opted in per file.
- Logging uses Kermit: `Logger.withTag("...")`.
- Compose resources (in `:composeApp`) are generated into the `dc.stashguard.resources` package.
