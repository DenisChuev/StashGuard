package dc.stashguard

import androidx.compose.ui.window.ComposeUIViewController
import dc.stashguard.di.initKoin

fun MainViewController() = ComposeUIViewController { App() }

// Called once from iOSApp.init() in Swift, mirroring StashGuardApp.onCreate() on Android.
fun setupKoin() = initKoin()
