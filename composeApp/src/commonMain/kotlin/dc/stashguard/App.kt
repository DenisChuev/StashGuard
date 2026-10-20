package dc.stashguard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dc.stashguard.core.ui.theme.StashGuardTheme
import dc.stashguard.navigation.AppNavigation

@Composable
@Preview
fun App() {
    StashGuardTheme {
        // AppNavigation's Scaffold handles the system bar insets itself
        AppNavigation()
    }
}
