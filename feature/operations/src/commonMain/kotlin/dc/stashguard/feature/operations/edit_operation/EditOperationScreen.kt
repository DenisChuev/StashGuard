package dc.stashguard.feature.operations.edit_operation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.core.common.formatForOperations
import dc.stashguard.core.common.toCurrencyString
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.feature.operations.add_operation.AddOperationContent
import dc.stashguard.feature.operations.add_operation.OperationTopAppBar
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.collections.emptyList

@Composable
fun EditOperationScreen(
    operationId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditOperationViewModel = koinViewModel { parametersOf(operationId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val operation by viewModel.operation.collectAsStateWithLifecycle()
    val isReadOnly by viewModel.isReadOnly.collectAsStateWithLifecycle()
    val availableAccounts by viewModel.availableAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Placeholder until the operation is loaded
    val operationType = operation?.type ?: OperationType.EXPENSE

    Scaffold(
        topBar = {
            OperationTopAppBar(
                operationType = operationType,
                onNavigateBack = onNavigateBack,
                isEditing = true,
                actions = {
                    if (operation != null) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !state.isLoading
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            val loadedOperation = operation
            if (isReadOnly && loadedOperation != null) {
                ReadOnlyOperationContent(
                    operation = loadedOperation,
                    error = state.error,
                    isLoading = state.isLoading,
                    onDelete = { showDeleteDialog = true },
                    modifier = Modifier.padding(paddingValues)
                )
            } else {
                AddOperationContent(
                    state = state,
                    operationType = operationType,
                    currentAccountId = operation?.accountId ?: "",
                    availableAccounts = availableAccounts,
                    availableCategories = availableCategories,
                    onAmountChange = viewModel::updateAmount,
                    onCategoryChange = viewModel::updateCategory,
                    onToAccountChange = viewModel::updateToAccount,
                    onDateChange = viewModel::updateDate,
                    onNoteChange = viewModel::updateNote,
                    onSave = { viewModel.saveOperation(onNavigateBack) },
                    modifier = Modifier.padding(paddingValues),
                    isIncomingTransfer = operation?.isIncoming == true,
                    isEditing = true,
                )
            }
        }
    )

    if (showDeleteDialog) {
        val isTransfer = operation?.type == OperationType.TRANSFER && !isReadOnly
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete operation?") },
            text = {
                Text(
                    if (isTransfer) {
                        "Both sides of the transfer will be deleted and both balances restored."
                    } else {
                        "The account balance will be restored."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteOperation(onSuccess = onNavigateBack)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/** A transfer whose other account was deleted: shown as-is, with deleting as the only action. */
@Composable
private fun ReadOnlyOperationContent(
    operation: Operation,
    error: String?,
    isLoading: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "This transfer can't be edited",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "The account on the other side of this transfer was deleted. " +
                            "You can still delete the transfer, which also restores this account's balance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val sign = if (operation.isIncoming) "+" else "-"
                DetailRow(label = "Amount", value = sign + operation.amount.toCurrencyString())
                DetailRow(label = "Date", value = operation.date.formatForOperations())
                if (operation.note.isNotBlank()) {
                    DetailRow(label = "Note", value = operation.note)
                }
            }
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onDelete,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Transfer", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
