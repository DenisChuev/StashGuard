// Compose helpers shared by the feature modules.
plugins {
    id("stashguard.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}
