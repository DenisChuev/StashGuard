package dc.stashguard.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OperationsScreen(
    onNavigateBack: () -> Unit
) {
    val sampleOperations = listOf(
        "Transfer to Savings - $500",
        "ATM Withdrawal - $100",
        "Online Payment - $75",
        "Deposit - $1,200",
        "Fee Charge - $5"
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Recent Operations",
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(sampleOperations) { operation ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillParentMaxWidth()
                    ) {
                        Text(
                            text = operation,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Back to Accounts")
            }
        }
    }
}