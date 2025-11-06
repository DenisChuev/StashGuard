@file:OptIn(ExperimentalMaterial3Api::class)

package dc.stashguard.screens.operations.add_operation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.model.Account
import dc.stashguard.model.OperationType
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddOperationScreen(
    accountId: String,
    operationType: OperationType,
    onNavigateBack: () -> Unit,
    viewModel: AddOperationViewModel = koinViewModel {
        parametersOf(accountId, operationType)
    }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val availableAccounts by viewModel.availableAccounts.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            OperationTopAppBar(
                operationType = operationType,
                onNavigateBack = onNavigateBack
            )
        },
        content = { paddingValues ->
            AddOperationContent(
                state = state,
                operationType = operationType,
                availableAccounts = availableAccounts,
                onAmountChange = viewModel::updateAmount,
                onCategoryChange = viewModel::updateCategory,
                onToAccountChange = viewModel::updateToAccount,
                onDateChange = viewModel::updateDate,
                onNoteChange = viewModel::updateNote,
                onSave = { viewModel.saveOperation(onNavigateBack) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}

@Composable
fun OperationTopAppBar(
    operationType: OperationType,
    onNavigateBack: () -> Unit
) {
    val (title, icon, color) = when (operationType) {
        OperationType.REVENUE -> Triple(
            "Add Revenue",
            Icons.AutoMirrored.Filled.TrendingUp,
            Color(0xFF4CAF50)
        )

        OperationType.EXPENSE -> Triple(
            "Add Expense",
            Icons.AutoMirrored.Filled.TrendingDown,
            Color(0xFFF44336)
        )

        OperationType.TRANSFER -> Triple(
            "Transfer Money",
            Icons.Default.SwapHoriz,
            Color(0xFF2196F3)
        )
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title)
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        }
    )
}

@Composable
fun AddOperationContent(
    state: OperationState,
    operationType: OperationType,
    availableAccounts: List<Account>,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onToAccountChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Amount Input
        AmountInputSection(
            amount = state.amount,
            onAmountChange = onAmountChange,
            operationType = operationType
        )

        // Category Input (for Revenue/Expense)
        if (operationType == OperationType.REVENUE ||
            operationType == OperationType.EXPENSE
        ) {
            CategoryInputSection(
                category = state.category,
                onCategoryChange = onCategoryChange
            )
        }

        // To Account Selection (for Transfer)
        if (operationType == OperationType.TRANSFER) {
            TransferAccountSection(
                toAccountId = state.toAccountId,
                availableAccounts = availableAccounts,
                currentAccountId = "", // You'll need to pass the current account ID
                onToAccountChange = onToAccountChange
            )
        }

        // Date Selection
        DateSelectionSection(
            date = state.date,
            onDateChange = onDateChange
        )

        // Note Input
        NoteInputSection(
            note = state.note,
            onNoteChange = onNoteChange
        )

        Spacer(modifier = Modifier.weight(1f))

        // Error Message
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Save Button
        SaveOperationButton(
            isLoading = state.isLoading,
            operationType = operationType,
            onSave = onSave,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AmountInputSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    operationType: OperationType,
    modifier: Modifier = Modifier
) {
    val label = when (operationType) {
        OperationType.REVENUE -> "Revenue Amount"
        OperationType.EXPENSE -> "Expense Amount"
        OperationType.TRANSFER -> "Transfer Amount"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    // Allow only numbers and decimal point
                    if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onAmountChange(newValue)
                    }
                },
                label = { Text("Enter amount") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, "Amount")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CategoryInputSection(
    category: String,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val commonCategories = listOf(
        "Food", "Transport", "Shopping", "Entertainment",
        "Bills", "Healthcare", "Salary", "Investment"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Category chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(commonCategories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom category input
            OutlinedTextField(
                value = category,
                onValueChange = onCategoryChange,
                label = { Text("Or enter custom category") },
                placeholder = { Text("Category name") },
                leadingIcon = {
                    Icon(Icons.Default.Category, "Category")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TransferAccountSection(
    toAccountId: String,
    availableAccounts: List<Account>,
    currentAccountId: String,
    onToAccountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredAccounts = availableAccounts.filter { it.id != currentAccountId }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Transfer To",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredAccounts.isEmpty()) {
                Text(
                    text = "No other accounts available for transfer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                filteredAccounts.forEach { account ->
                    AccountSelectionItem(
                        account = account,
                        isSelected = toAccountId == account.id,
                        onSelect = { onToAccountChange(account.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccountSelectionItem(
    account: Account,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(account.color, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "$${account.balance}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DateSelectionSection(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Date",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = date.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select date"
                )
            }
        }
    }

    // Simple date picker dialog (you might want to use a proper date picker library)
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Date") },
            text = {
                // For now, just show current date
                // In a real app, you'd use a proper date picker component
                Text("Selected: ${date.toString()}")
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun NoteInputSection(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Note (Optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("Add a note...") },
                placeholder = { Text("Enter any additional information") },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.Notes, "Note")
                },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SaveOperationButton(
    isLoading: Boolean,
    operationType: OperationType,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonText = when (operationType) {
        OperationType.REVENUE  -> "Add Revenue"
        OperationType.EXPENSE -> "Add Expense"
        OperationType.TRANSFER -> "Complete Transfer"
    }

    val buttonColors = when (operationType) {
        OperationType.REVENUE  -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50)
        )

        OperationType.EXPENSE -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF44336)
        )

        OperationType.TRANSFER-> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3)
        )
    }

    Button(
        onClick = onSave,
        modifier = modifier,
        enabled = !isLoading,
        colors = buttonColors
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Saving...")
        } else {
            Text(buttonText, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = when {
            selected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = when {
            selected -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = MaterialTheme.shapes.small,
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            label()
        }
    }
}