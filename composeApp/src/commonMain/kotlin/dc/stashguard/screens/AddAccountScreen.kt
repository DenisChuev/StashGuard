package dc.stashguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    onAccountAdded: (Account) -> Unit,
    onNavigateBack: () -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var accountBalance by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF2196F3)) } // Default blue
    var isDebt by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var showError by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Name Input
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Name") },
                placeholder = { Text("e.g., Savings, Credit Card") },
                modifier = Modifier.fillMaxWidth()
            )

            // Balance Input
            OutlinedTextField(
                value = accountBalance,
                onValueChange = { newValue ->
                    // Allow only numbers and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("-?\\d*\\.?\\d*"))) {
                        accountBalance = newValue
                    }
                },
                label = { Text("Balance") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth()
            )

            // Debt Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = isDebt,
                    onCheckedChange = { isDebt = it }
                )
                Text("This is a debt account")
            }

            // Color Selection
            Text("Select Account Color")
            ColorSelectionGrid(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            // Error Message
            if (showError) {
                Text(
                    text = "Please fill in account name and balance",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Add Button
            Button(
                onClick = {
                    if (accountName.isNotBlank() && accountBalance.isNotBlank()) {
                        val balanceValue = accountBalance.toDoubleOrNull() ?: 0.0
                        val newAccount = Account(
                            name = accountName,
                            balance = balanceValue,
                            color = selectedColor,
                            isDebt = isDebt
                        )
                        showError = false
                        onAccountAdded(newAccount)
                        onNavigateBack()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Account")
            }
        }
    }
}

@Composable
fun ColorSelectionGrid(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFFFF4081), // Pink
        Color(0xFFE91E63), // Red
        Color(0xFFFFC107), // Amber
        Color(0xFF2196F3), // Blue
        Color(0xFF64B5F6), // Light Blue
        Color(0xFF4CAF50), // Green
        Color(0xFF9E9E9E), // Gray
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF795548), // Brown
        Color(0xFF9C27B0)  // Purple
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .then(
                        if (color == selectedColor) {
                            Modifier.border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                        } else Modifier
                    )
            )
        }
    }
}