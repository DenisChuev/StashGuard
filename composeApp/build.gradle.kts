// App shell shared by Android and iOS: navigation, DI wiring and the iOS framework.
// Screens live in :feature:*, business logic in :core:domain, persistence in :core:data and :core:database.
plugins {
    id("stashguard.kmp.compose")
    alias(libs.plugins.kotlin.serialization)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "dc.stashguard.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "dc.stashguard.shared"

        // Needed for Compose Multiplatform resources on Android.
        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

//    js {
//        browser()
//        binaries.executable()
//    }
//
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser()
//        binaries.executable()
//    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.database)
            implementation(projects.core.data)
            implementation(projects.feature.accounts)
            implementation(projects.feature.operations)
            implementation(projects.feature.categories)

            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.material.icons)
            api(libs.koin.core)
            implementation(libs.kermit)
        }
//        commonTest.dependencies {
//            implementation(libs.kotlin.test)
//        }
    }
}
