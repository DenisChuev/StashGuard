@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)

package dc.stashguard.screens.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.model.Operation
import dc.stashguard.model.OperationType
import dc.stashguard.screens.operations.add_operation.FilterChip
import dc.stashguard.util.formatForOperations
import dc.stashguard.util.formatRelativeTime
import dc.stashguard.util.toBalanceString
import kotlinx.datetime.format
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun OperationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OperationsViewModel = koinViewModel()
) {
    val operations by viewModel.operations.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf<OperationType?>(null) }

    val filteredOperations = remember(operations, searchQuery, typeFilter) {
        operations.filter { operation ->
            val matchesSearch = searchQuery.isBlank() ||
                    operation.category.contains(searchQuery, ignoreCase = true) ||
                    operation.note.contains(searchQuery, ignoreCase = true)

            val matchesType = typeFilter == null || operation.type == typeFilter

            matchesSearch && matchesType
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Operations") },
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
            // Header with type-based stats
            OperationsHeader(operations = operations)

            // Enhanced filter with type filtering
            OperationsFilterBar(
                onSearchChanged = { searchQuery = it },
                onFilterChanged = { typeFilter = it }
            )

            // Operations list
            if (filteredOperations.isEmpty()) {
                OperationsEmptyState(
                    modifier = Modifier.weight(1f),
                    hasOperations = operations.isNotEmpty()
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredOperations) { operation ->
                        OperationItem(operation = operation)
                    }
                }
            }
        }
    }
}

@Composable
fun OperationsEmptyState(
    modifier: Modifier = Modifier,
    hasOperations: Boolean = false
) {
    Column(
        modifier = modifier,
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
            text = if (hasOperations) "Try changing your filters" else "Your transactions will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun OperationItem(
    operation: Operation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on operation type
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (operation.type) {
                            OperationType.REVENUE -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            OperationType.EXPENSE -> Color(0xFFF44336).copy(alpha = 0.2f)
                            OperationType.TRANSFER -> Color(0xFF2196F3).copy(alpha = 0.2f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (operation.type) {
                    OperationType.REVENUE -> Icons.AutoMirrored.Filled.CallReceived
                    OperationType.EXPENSE -> Icons.AutoMirrored.Filled.CallMade
                    OperationType.TRANSFER -> Icons.Default.SwapHoriz
                }
                val tint = when (operation.type) {
                    OperationType.REVENUE -> Color(0xFF4CAF50)
                    OperationType.EXPENSE -> Color(0xFFF44336)
                    OperationType.TRANSFER -> Color(0xFF2196F3)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = operation.type.name,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Operation details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = operation.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (operation.note.isNotEmpty()) {
                    Text(
                        text = operation.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Use the new date formatting
                Text(
                    text = operation.date.formatForOperations(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                // Optional: Add relative time for very recent operations
                if (operation.createdAt.isRecent()) {
                    Text(
                        text = operation.createdAt.formatRelativeTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Amount with type-specific formatting
            Text(
                text = when (operation.type) {
                    OperationType.REVENUE -> "+${operation.amount.toBalanceString()}"
                    OperationType.EXPENSE -> "-${operation.amount.toBalanceString()}"
                    OperationType.TRANSFER -> "→ ${operation.amount.toBalanceString()}"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = when (operation.type) {
                    OperationType.REVENUE -> Color(0xFF4CAF50)
                    OperationType.EXPENSE -> Color(0xFFF44336)
                    OperationType.TRANSFER -> Color(0xFF2196F3)
                }
            )
        }
    }
}

private fun Instant.isRecent(hours: Int = 24): Boolean {
    val duration = Clock.System.now() - this
    return duration.inWholeHours < hours
}

@Composable
fun OperationsHeader(
    operations: List<Operation>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.primary)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Recent Operations",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

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
                text = value.toBalanceString(),
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
                placeholder = { Text("Search operations...") },
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