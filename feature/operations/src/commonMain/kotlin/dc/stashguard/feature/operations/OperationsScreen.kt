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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.core.common.toCurrencyString
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.ui.OperationDayHeader
import dc.stashguard.core.ui.OperationRow
import dc.stashguard.core.ui.color
import dc.stashguard.feature.operations.category_stats.CategorySlice
import dc.stashguard.feature.operations.category_stats.DonutChart
import dc.stashguard.feature.operations.category_stats.categorySlices
import dc.stashguard.core.ui.transferAccounts
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OperationsScreen(
    onEditOperation: (accountId: String, operationId: String, operationType: OperationType) -> Unit,
    onNavigateToCategoryStats: (OperationType) -> Unit,
    onNavigateToAccounts: () -> Unit,
    viewModel: OperationsViewModel = koinViewModel()
) {
    val loadedOperations by viewModel.operations.collectAsStateWithLifecycle()
    val operations = loadedOperations ?: emptyList()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accountNames by viewModel.accountNames.collectAsStateWithLifecycle()
    var isSearching by remember { mutableStateOf(false) }
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
            OperationsTopBar(
                // With nothing recorded there is nothing to search or filter
                hasOperations = operations.isNotEmpty(),
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchQueryChanged = { searchQuery = it },
                onOpenSearch = { isSearching = true },
                onCloseSearch = {
                    isSearching = false
                    searchQuery = ""
                },
                onFilterChanged = { typeFilter = it }
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
                    typeFilter = typeFilter,
                    onFilterChanged = { typeFilter = it },
                    onEditOperation = onEditOperation,
                    onNavigateToCategoryStats = onNavigateToCategoryStats,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Totals, the selected filter and the list, once there is at least one operation. */
@Composable
private fun OperationsContent(
    operations: List<Operation>,
    filteredOperations: List<Operation>,
    categories: Map<String, Category>,
    accountNames: Map<String, String>,
    typeFilter: OperationType?,
    onFilterChanged: (OperationType?) -> Unit,
    onEditOperation: (accountId: String, operationId: String, operationType: OperationType) -> Unit,
    onNavigateToCategoryStats: (OperationType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header with type-based stats
        OperationsHeader(
            operations = operations,
            categories = categories,
            onTotalClick = onNavigateToCategoryStats
        )

        typeFilter?.let { filter ->
            FilterChip(
                selected = true,
                onClick = { onFilterChanged(null) },
                label = { Text(filter.label) },
                trailingIcon = { Icon(Icons.Default.Close, "Clear filter") },
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }

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
                        // Only the day's spending, as a negative amount like the expense rows; none if nothing was spent
                        val expenses = dayOperations.filter { it.type == OperationType.EXPENSE }.map { it.amount }
                        OperationDayHeader(date = date, total = if (expenses.isEmpty()) null else -expenses.sum())
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
    categories: Map<String, Category>,
    onTotalClick: (OperationType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.primary)) {
        // Type-based statistics
        val income = operations.filter { it.type == OperationType.REVENUE }.sumOf { it.amount }
        val expense = operations.filter { it.type == OperationType.EXPENSE }.sumOf { it.amount }
        val incomeSlices = remember(operations, categories) { operations.categorySlices(categories, OperationType.REVENUE) }
        val expenseSlices = remember(operations, categories) { operations.categorySlices(categories, OperationType.EXPENSE) }

        Row(modifier = Modifier.padding(16.dp)) {
            StatItem(
                label = "Income",
                value = income,
                color = Color(0xFF4CAF50),
                slices = incomeSlices,
                onClick = { onTotalClick(OperationType.REVENUE) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatItem(
                label = "Expense",
                value = expense,
                color = Color(0xFFF44336),
                slices = expenseSlices,
                onClick = { onTotalClick(OperationType.EXPENSE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** A total that opens its breakdown by category, previewed by a mini chart of [slices] on the right. */
@Composable
private fun StatItem(
    label: String,
    value: Double,
    color: Color,
    slices: List<CategorySlice>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        // Chart and total sit together in the middle of the card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val outline = MaterialTheme.colorScheme.outline
            DonutChart(
                slices = slices,
                colorOf = { it.category?.color ?: outline },
                size = 36.dp,
                strokeWidth = 7.dp,
                showPercentages = false
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
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
}

/**
 * The title bar, which also holds search and the type filter. While searching, the title turns into
 * the search field, and going back closes it and clears the query.
 */
@Composable
private fun OperationsTopBar(
    hasOperations: Boolean,
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onFilterChanged: (OperationType?) -> Unit
) {
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
    )
    val actions: @Composable () -> Unit = {
        if (hasOperations) {
            if (isSearching) {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, "Clear search")
                    }
                }
            } else {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Default.Search, "Search")
                }
            }
            TypeFilterMenu(onFilterChanged = onFilterChanged)
        }
    }

    if (isSearching && hasOperations) {
        // Left-aligned, so the search field gets the whole width between the icons
        TopAppBar(
            title = { SearchField(query = searchQuery, onQueryChanged = onSearchQueryChanged) },
            navigationIcon = {
                IconButton(onClick = onCloseSearch) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close search")
                }
            },
            actions = { actions() },
            colors = colors
        )
    } else {
        CenterAlignedTopAppBar(
            // A tab root, so there is nothing to go back to
            title = { Text("History") },
            actions = { actions() },
            colors = colors
        )
    }
}

/** A borderless field on the primary-colored bar, focused as soon as search opens. */
@Composable
private fun SearchField(
    query: String,
    onQueryChanged: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    TextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text("Search history...") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = onPrimary,
            unfocusedTextColor = onPrimary,
            cursorColor = onPrimary,
            focusedPlaceholderColor = onPrimary.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = onPrimary.copy(alpha = 0.7f)
        ),
        modifier = Modifier.focusRequester(focusRequester)
    )
}

/** Filter icon with a menu of the types History lists. */
@Composable
private fun TypeFilterMenu(
    onFilterChanged: (OperationType?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.FilterList, "Filter by type")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Types") },
                onClick = {
                    onFilterChanged(null)
                    expanded = false
                }
            )
            FILTER_TYPES.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label) },
                    onClick = {
                        onFilterChanged(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Transfers stay in the list but are not offered as a filter. */
private val FILTER_TYPES = listOf(OperationType.REVENUE, OperationType.EXPENSE)

/** Matches the header's totals. */
private val OperationType.label: String
    get() = when (this) {
        OperationType.REVENUE -> "Income"
        OperationType.EXPENSE -> "Expense"
        OperationType.TRANSFER -> "Transfer"
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
