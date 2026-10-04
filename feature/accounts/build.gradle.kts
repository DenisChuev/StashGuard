plugins {
    id("stashguard.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.reorderable)
        }
    }
}
