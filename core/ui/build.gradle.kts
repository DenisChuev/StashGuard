// Compose helpers shared by the feature modules.
plugins {
    id("stashguard.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(libs.material.icons)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
    }
}
