package dc.stashguard.screens.accounts.edit_account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import dc.stashguard.screens.accounts.add_account.ColorSelectionGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    accountId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditAccountViewModel = koinViewModel { parametersOf(accountId) }
) {
    // Collect all necessary state from the ViewModel
    val account by viewModel.account.collectAsState()
    val editingName by viewModel.editingAccountName.collectAsState()
    val editingBalance by viewModel.editingAccountBalance.collectAsState()
    val editingColor by viewModel.editingAccountColor.collectAsState()
    val editingIsDebt by viewModel.editingAccountIsDebt.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Use a stateful screen pattern or handle loading/error directly here
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Error: $error")
                Button(onClick = { viewModel.loadAccountDetails() }) { // Retry loading
                    Text("Retry")
                }
            }
        }
        return
    }

    // If account is null after loading, handle it (shouldn't happen if loaded successfully)
    if (account == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Account not found.")
            return
        }
    }

    // State for color selection dialog (optional, if you want a picker)
    var showColorPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveEditedAccount() }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }

                    IconButton(
                        onClick = {
                            viewModel.deleteAccount(onSuccess = {
                                onNavigateBack()
                            })
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
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
            // Account Name Input
            OutlinedTextField(
                value = editingName,
                onValueChange = { viewModel.updateEditingName(it) },
                label = { Text("Account Name") },
                placeholder = { Text("e.g., Savings, Credit Card") },
                modifier = Modifier.fillMaxWidth()
            )

            // Balance Input
            OutlinedTextField(
                value = editingBalance,
                onValueChange = { newValue ->
                    // Allow only numbers, negative sign, and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("-?\\d*\\.?\\d*"))) {
                        viewModel.updateEditingBalance(newValue)
                    }
                },
                label = { Text("Balance") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Debt Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = editingIsDebt,
                    onCheckedChange = { viewModel.updateEditingIsDebt(it) }
                )
                Text("This is a debt account")
            }

            // Color Selection (Example with a simple clickable box)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Color:")
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(editingColor, shape = RoundedCornerShape(8.dp))
                        .clickable { showColorPicker = true } // Show picker dialog
                )
            }

            // Example: Show Color Picker Dialog (you can reuse ColorSelectionGrid here)
            if (showColorPicker) {
                AlertDialog(
                    onDismissRequest = { showColorPicker = false },
                    title = { Text("Select Account Color") },
                    text = {
                        ColorSelectionGrid( // Assuming you have this composable from AddAccountScreen
                            selectedColor = editingColor,
                            onColorSelected = {
                                viewModel.updateEditingColor(it)
                                showColorPicker = false
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showColorPicker = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Error Message (if any during save attempt)
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}