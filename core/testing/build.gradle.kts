// Test doubles shared by the test source sets: in-memory fake repositories, test data builders
// and the main-dispatcher setup for ViewModel tests. Only ever a test dependency.
plugins {
    id("stashguard.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.domain)
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
        }
        // kotlin-test's annotations need a test framework on the JVM; test source sets pick it
        // automatically, but this is a main source set
        androidMain.dependencies {
            api(libs.kotlin.testJunit)
        }
    }
}
