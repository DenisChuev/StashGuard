package dc.stashguard

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dc.stashguard.navigation.AppNavigation

@Composable
@Preview
fun App() {
    MaterialTheme {
        // AppNavigation's Scaffold handles the system bar insets itself
        AppNavigation()
    }
}
