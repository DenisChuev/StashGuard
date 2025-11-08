@file:OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)

package dc.stashguard.screens.accounts.add_account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dc.stashguard.model.Account
import dc.stashguard.util.formatBalanceWithSpaces
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.min
import kotlin.time.ExperimentalTime

private val logger = Logger.withTag("AddAccountScreen")

@Composable
fun AddAccountScreen(
    onAccountAdded: (Account) -> Unit = {},
    onNavigateBack: () -> Unit,
) {
    val viewModel: AddAccountViewModel = koinViewModel()
    var accountName by remember { mutableStateOf("") }
    var accountBalance by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF2196F3)) } // Default blue
    var isDebt by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
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
                BalanceInput(
                    balance = accountBalance,
                    onBalanceChange = { newBalance -> accountBalance = newBalance },
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
            }

            // Add Button
            Button(
                onClick = {
                    logger.d("Add account onclick")
                    if (accountName.isNotBlank() && accountBalance.isNotBlank()) {
                        val balanceValue = accountBalance.toDoubleOrNull() ?: 0.0
                        val newAccount = Account(
                            name = accountName,
                            balance = balanceValue,
                            color = selectedColor,
                            isDebt = isDebt
                        )
                        logger.d("Try to add new account: $newAccount")

                        showError = false
                        viewModel.addAccount(
                            account = newAccount,
                            onSuccess = {
                                onAccountAdded(newAccount)
                                onNavigateBack()
                            })
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
fun BalanceInput(
    balance: String,
    onBalanceChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = balance,
        onValueChange = { newValue ->
            if (isValidBalanceInput(newValue)) {
                onBalanceChange(newValue)
            }
        },
        visualTransformation = BalanceVisualTransformation(),
        label = { Text("Balance") },
        placeholder = { Text("0.00") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            autoCorrectEnabled = false
        ),
        singleLine = true,
        isError = !isValidBalanceFinal(balance) && balance.isNotEmpty(),
        supportingText = {
            if (!isValidBalanceFinal(balance) && balance.isNotEmpty()) {
                Text("Must be between -1B and 1B with max 2 decimal places")
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

class BalanceVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val formatted = formatBalanceWithSpaces(original)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= original.length) return formatted.length

                val nonSpaceCountInOriginal = original.take(offset).count { it != ' ' }
                var currentNonSpaceCount = 0
                for (i in formatted.indices) {
                    if (formatted[i] != ' ') {
                        currentNonSpaceCount++
                    }
                    if (currentNonSpaceCount >= nonSpaceCountInOriginal) {
                        return i + 1
                    }
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= formatted.length) return original.length

                val nonSpaceCountInFormatted = formatted.take(offset).count { it != ' ' }
                return nonSpaceCountInFormatted.coerceAtMost(original.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

// Validation for real-time input
private fun isValidBalanceInput(input: String): Boolean {
    if (input.isEmpty()) return true

    // Allow: optional minus, digits, optional single decimal with 0-2 digits after
    if (!input.replace(" ", "").matches(Regex("-?\\d*\\.?\\d{0,2}"))) {
        return false
    }

    // Prevent multiple decimal points
    if (input.count { it == '.' } > 1) {
        return false
    }

    // Prevent negative with decimal right after minus
    if (input.startsWith("-.")) {
        return false
    }

    return true
}

// Final validation for the complete value
private fun isValidBalanceFinal(input: String): Boolean {
    if (input.isEmpty()) return true

    return try {
        val value = input.replace(" ", "").toDouble()
        value in -1000000000.0..1000000000.0
    } catch (e: NumberFormatException) {
        false
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