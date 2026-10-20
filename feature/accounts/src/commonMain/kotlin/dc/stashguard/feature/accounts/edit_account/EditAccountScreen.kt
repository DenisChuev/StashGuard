package dc.stashguard.feature.accounts.edit_account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dc.stashguard.feature.accounts.add_account.BalanceInput
import dc.stashguard.feature.accounts.add_account.ColorSelectionGrid
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(
    accountId: String,
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: EditAccountViewModel = koinViewModel { parametersOf(accountId) }
) {
    // Collect all necessary state from the ViewModel
    val account by viewModel.account.collectAsState()
    val editingName by viewModel.editingAccountName.collectAsState()
    val editingBalance by viewModel.editingAccountBalance.collectAsState()
    val editingColor by viewModel.editingAccountColor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    // Use a stateful screen pattern or handle loading/error directly here
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
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
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete account?") },
            text = {
                Text(
                    "\"${account?.name}\" and all its operations will be deleted. " +
                        "Transfers with other accounts stay in those accounts. This can't be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteAccount(onSuccess = onAccountDeleted)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(),
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
                        onClick = { showDeleteConfirmation = true }
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
            Column(
                modifier = Modifier.weight(1f),
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
                BalanceInput(
                    balance = editingBalance,
                    onBalanceChange = { newBalance -> viewModel.updateEditingBalance(newBalance) },
                    modifier = Modifier.fillMaxWidth()
                )

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

            // Same look as the Add Account button
            Button(
                onClick = { viewModel.saveEditedAccount(onSuccess = onNavigateBack) },
                enabled = !isSaving,
                shape = MaterialTheme.shapes.medium,
                // As tall as the text fields above
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save")
            }
        }
    }
}