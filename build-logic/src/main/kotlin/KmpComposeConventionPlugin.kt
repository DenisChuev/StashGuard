import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** [KmpLibraryConventionPlugin] plus Compose Multiplatform. */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("stashguard.kmp.library")
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            commonMainDependencies {
                implementation(libs.library("compose-runtime"))
                implementation(libs.library("compose-foundation"))
                implementation(libs.library("compose-material3"))
                implementation(libs.library("compose-ui"))
                implementation(libs.library("compose-uiToolingPreview"))
            }
        }
    }
}
