// Pure Kotlin business layer: models, repository contracts and use cases.
// No Android, Compose, Room or Koin dependencies.
plugins {
    id("stashguard.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
    }
}
