// Implements the :core:domain repository contracts on top of :core:database.
plugins {
    id("stashguard.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.database)
            implementation(libs.koin.core)
        }
    }
}
