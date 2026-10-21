@file:OptIn(ExperimentalMaterial3Api::class)

package dc.stashguard.feature.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.core.common.toCurrencyString
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.ui.OperationDayHeader
import dc.stashguard.core.ui.OperationRow
import dc.stashguard.core.ui.transferAccounts
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OperationsScreen(
    onEditOperation: (accountId: String, operationId: String, operationType: OperationType) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    viewModel: OperationsViewModel = koinViewModel()
) {
    val loadedOperations by viewModel.operations.collectAsStateWithLifecycle()
    val operations = loadedOperations ?: emptyList()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accountNames by viewModel.accountNames.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf<OperationType?>(null) }

    val filteredOperations = remember(operations, categories, accountNames, searchQuery, typeFilter) {
        operations.filter { operation ->
            val matchesSearch = searchQuery.isBlank() ||
                    operation.searchableNames(categories, accountNames).any { it.contains(searchQuery, ignoreCase = true) } ||
                    operation.note.contains(searchQuery, ignoreCase = true)

            val matchesType = typeFilter == null || operation.type == typeFilter

            matchesSearch && matchesType
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Still loading: show nothing rather than a flash of the empty state
                loadedOperations == null -> Unit

                // Nothing recorded yet: no totals or search, just the empty state
                operations.isEmpty() -> OperationsEmptyState(
                    modifier = Modifier.weight(1f),
                    hasOperations = false,
                    onGoToAccounts = onNavigateToAccounts
                )

                else -> OperationsContent(
                    operations = operations,
                    filteredOperations = filteredOperations,
                    categories = categories,
                    accountNames = accountNames,
                    onSearchChanged = { searchQuery = it },
                    onFilterChanged = { typeFilter = it },
                    onEditOperation = onEditOperation,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Totals, search and the list, once there is at least one operation. */
@Composable
private fun OperationsContent(
    operations: List<Operation>,
    filteredOperations: List<Operation>,
    categories: Map<String, Category>,
    accountNames: Map<String, String>,
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (OperationType?) -> Unit,
    onEditOperation: (accountId: String, operationId: String, operationType: OperationType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header with type-based stats
        OperationsHeader(operations = operations)

        // Enhanced filter with type filtering
        OperationsFilterBar(
            onSearchChanged = onSearchChanged,
            onFilterChanged = onFilterChanged
        )

        // Operations list
        if (filteredOperations.isEmpty()) {
            OperationsEmptyState(
                modifier = Modifier.weight(1f),
                hasOperations = true
            )
        } else {
            // Laid out like an account's history: rows grouped under their day
            val operationsByDay = remember(filteredOperations) { filteredOperations.groupBy { it.date } }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                operationsByDay.forEach { (date, dayOperations) ->
                    item(key = "day_$date") {
                        OperationDayHeader(date = date, total = dayOperations.sumOf { it.netAmount })
                    }
                    items(dayOperations, key = { it.id }) { operation ->
                        OperationRow(
                            operation = operation,
                            category = categories[operation.category],
                            accountNames = accountNames,
                            onClick = { onEditOperation(operation.accountId, operation.id, operation.type) },
                            // Across all accounts a transfer only moves money, so it has no sign
                            signTransfers = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OperationsEmptyState(
    modifier: Modifier = Modifier,
    hasOperations: Boolean = false,
    onGoToAccounts: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasOperations) "No matching operations" else "No operations yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (hasOperations) {
                "Try changing your filters"
            } else {
                "Open an account to add a revenue, expense or transfer"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
        if (onGoToAccounts != null) {
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(onClick = onGoToAccounts) {
                Text("Go to Accounts")
            }
        }
    }
}

@Composable
fun OperationsHeader(
    operations: List<Operation>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.primary)) {
        // Type-based statistics
        val income = operations.filter { it.type == OperationType.REVENUE }.sumOf { it.amount }
        val expense = operations.filter { it.type == OperationType.EXPENSE }.sumOf { it.amount }
        val transfer = operations.filter { it.type == OperationType.TRANSFER }.sumOf { it.amount }

        Row(modifier = Modifier.padding(16.dp)) {
            StatItem(
                label = "Income",
                value = income,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatItem(
                label = "Expense",
                value = expense,
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatItem(
                label = "Transfer",
                value = transfer,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value.toCurrencyString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun OperationsFilterBar(
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (OperationType?) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<OperationType?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search field
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    onSearchChanged(it)
                },
                placeholder = { Text("Search history...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Type filter dropdown
            Box {
                IconButton(
                    onClick = { expanded = true }
                ) {
                    Icon(Icons.Default.FilterList, "Filter by type")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Types") },
                        onClick = {
                            selectedFilter = null
                            onFilterChanged(null)
                            expanded = false
                        }
                    )
                    OperationType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(type.name.replaceFirstChar { it.uppercase() })
                            },
                            onClick = {
                                selectedFilter = type
                                onFilterChanged(type)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Selected filter chip
        selectedFilter?.let { filter ->
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                FilterChip(
                    selected = true,
                    onClick = {
                        selectedFilter = null
                        onFilterChanged(null)
                    },
                    label = {
                        Text(filter.name.replaceFirstChar { it.uppercase() })
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Close, "Clear filter")
                    }
                )
            }
        }
    }
}

/** Names the search matches besides the note: the category's, or a transfer's two accounts. */
private fun Operation.searchableNames(
    categories: Map<String, Category>,
    accountNames: Map<String, String>
): List<String> =
    if (type == OperationType.TRANSFER) {
        transferAccounts(this, accountNames).toList()
    } else {
        listOfNotNull(categories[category]?.name)
    }

/** The operation's effect on the total across all accounts: a transfer moves money between them. */
private val Operation.netAmount: Double
    get() = when (type) {
        OperationType.REVENUE -> amount
        OperationType.EXPENSE -> -amount
        OperationType.TRANSFER -> 0.0
    }
