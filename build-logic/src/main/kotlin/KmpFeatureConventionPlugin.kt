import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Presentation-layer feature module: Compose screens and ViewModels.
 * A feature depends on the domain layer only, never on `:core:data` or `:core:database`.
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("stashguard.kmp.compose")

        extensions.configure<KotlinMultiplatformExtension> {
            commonMainDependencies {
                implementation(project(":core:common"))
                implementation(project(":core:domain"))
                implementation(project(":core:ui"))

                implementation(libs.library("androidx-lifecycle-viewmodelCompose"))
                implementation(libs.library("androidx-lifecycle-runtimeCompose"))
                implementation(libs.library("material-icons"))
                implementation(libs.library("koin-core"))
                implementation(libs.library("koin-compose"))
                implementation(libs.library("koin-compose-viewmodel"))
                implementation(libs.library("kermit"))
                implementation(libs.library("kotlinx-datetime"))
            }
        }
    }
}
