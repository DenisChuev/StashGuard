@file:OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)

package dc.stashguard.feature.accounts.add_account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dc.stashguard.core.common.CURRENCY_SYMBOL
import dc.stashguard.core.common.toBalanceDouble
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.ui.BalanceVisualTransformation
import org.koin.compose.viewmodel.koinViewModel
import kotlin.let
import kotlin.math.min
import kotlin.time.ExperimentalTime

private val logger = Logger.withTag("AddAccountScreen")

/** The theme's indigo (primaryContainer). */
val DefaultAccountColor = Color(0xFF3F51B5)

/**
 * Twelve clearly different hues, three rows of four. Account cards always draw their text in
 * white; all but the yellow keep at least 3:1 contrast with it (the WCAG AA level for large or
 * bold text).
 */
private val accountColors = listOf(
    DefaultAccountColor,
    Color(0xFF0288D1), // Light Blue 700, a sky blue
    Color(0xFF00897B), // Teal 600
    Color(0xFF43A047), // Green 600
    Color(0xFFFBC02D), // Yellow 700
    Color(0xFFEF6C00), // Orange 800
    Color(0xFFF44336), // Red 500
    Color(0xFFFF4081), // Pink A200
    Color(0xFFAB47BC), // Purple 400
    Color(0xFF8E2442), // Burgundy
    Color(0xFF8D6E63), // Brown 400
    Color(0xFF37474F)  // Blue Grey 800, a graphite
)

@Composable
fun AddAccountScreen(
    onAccountAdded: (Account) -> Unit = {},
    onNavigateBack: () -> Unit,
) {
    val viewModel: AddAccountViewModel = koinViewModel()
    var accountName by remember { mutableStateOf("") }
    var accountBalance by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(DefaultAccountColor) }
    var showError by remember { mutableStateOf(false) }
    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.error.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Add Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
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
                AccountNameInput(
                    name = accountName,
                    onNameChange = {
                        accountName = it
                        viewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Balance Input
                BalanceInput(
                    balance = accountBalance,
                    onBalanceChange = { newBalance ->
                        accountBalance = newBalance
                        viewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Color Selection
                Text("Select Account Color")
                ColorSelectionGrid(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )

                // Error Message
                if (showError) {
                    Text(
                        text = "Please enter an account name",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                saveError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Add Button
            Button(
                onClick = {
                    logger.d("Add account onclick")
                    // Same parsing and range check as the balance field's error state; empty means 0
                    val balanceValue = accountBalance.ifBlank { "0" }.toBalanceDouble()
                    if (accountName.isNotBlank() && balanceValue != null && isValidBalanceFinal(accountBalance)) {
                        val newAccount = Account(
                            name = accountName.trim(),
                            balance = balanceValue,
                            colorArgb = selectedColor.toArgb()
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
                enabled = !isSaving,
                shape = MaterialTheme.shapes.medium,
                // As tall as the text fields above
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Add Account")
            }
        }
    }
}

@Composable
fun AccountNameInput(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Account name") },
        placeholder = { Text("e.g. Savings, Credit Card") },
        leadingIcon = { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    )
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
        placeholder = { Text("0") },
        leadingIcon = { Icon(Icons.Outlined.Payments, contentDescription = null) },
        suffix = { Text(CURRENCY_SYMBOL) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        isError = !isValidBalanceFinal(balance) && balance.isNotEmpty(),
        supportingText = {
            if (!isValidBalanceFinal(balance) && balance.isNotEmpty()) {
                Text("Must be between -1B and 1B with max 2 decimal places")
            }
        },
        modifier = modifier.fillMaxWidth()
    )
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
        return input.toBalanceDouble()?.let { it in -1000000000.0..1000000000.0 } == true
    } catch (e: NumberFormatException) {
        false
    }
}

@Composable
fun ColorSelectionGrid(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(accountColors) { color ->
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