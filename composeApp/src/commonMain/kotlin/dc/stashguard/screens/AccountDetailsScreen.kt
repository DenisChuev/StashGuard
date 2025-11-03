package dc.stashguard.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountDetailsScreen(
    accountId: String,
    onNavigateBack: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Account Details: $accountId",
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Balance: $2,450.00",
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Account Number: ****1234",
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Back to Accounts")
            }
        }
    }
}