plugins {
    `kotlin-dsl`
}

dependencies {
    // compileOnly: the plugins themselves are put on the classpath by the root build script
    // (`apply false`), so every module shares one copy of them.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "stashguard.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "stashguard.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("kmpFeature") {
            id = "stashguard.kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
    }
}
