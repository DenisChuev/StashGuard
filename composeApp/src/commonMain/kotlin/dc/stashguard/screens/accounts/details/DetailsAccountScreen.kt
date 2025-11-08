@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)

package dc.stashguard.screens.accounts.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.model.Account
import dc.stashguard.model.Operation
import dc.stashguard.model.OperationType
import dc.stashguard.util.toBalanceString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

@Composable
fun DetailsAccountScreen(
    modifier: Modifier = Modifier,
    accountId: String,
    onNavigateToEditAccount: (String) -> Unit,
    onNavigateAddOperation: (String, OperationType) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DetailsAccountViewModel = koinViewModel { parametersOf(accountId) }
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val recentOperations by viewModel.recentOperations.collectAsStateWithLifecycle()
    val statistics by viewModel.accountStatistics.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DetailsTopAppBar(
                onNavigateBack = onNavigateBack,
                onEditAccount = { onNavigateToEditAccount(accountId) },
                isLoading = isLoading,
                account = account
            )
        },
        floatingActionButton = {
            if (account != null) {
                ExtendedFloatingActionButton(
                    onClick = { onNavigateAddOperation(accountId, OperationType.REVENUE) },
                    icon = { Icon(Icons.Default.Add, "Add Operation") },
                    text = { Text("Add Operation") }
                )
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))
            error != null -> ErrorState(
                error = error!!,
                onRetry = { viewModel.refresh() },
                modifier = Modifier.padding(paddingValues)
            )

            account != null -> AccountDetailsContent(
                account = account!!,
                recentOperations = recentOperations,
                statistics = statistics,
                onAddRevenue = { onNavigateAddOperation(accountId, OperationType.REVENUE) },
                onAddExpense = { onNavigateAddOperation(accountId, OperationType.EXPENSE) },
                onTransfer = { onNavigateAddOperation(accountId, OperationType.TRANSFER) },
                modifier = Modifier.padding(paddingValues)
            )

            else -> EmptyState(modifier = Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading account details...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DetailsTopAppBar(
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    isLoading: Boolean,
    account: Account?,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                account?.name ?: "Account Details",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                enabled = !isLoading
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        },
        actions = {
            if (account != null) {
                IconButton(
                    onClick = onEditAccount,
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Edit, "Edit Account")
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun AccountDetailsContent(
    account: Account,
    recentOperations: List<Operation>,
    statistics: AccountStatistics,
    onAddRevenue: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Account Summary Card
        AccountSummaryCard(account = account)

        // Statistics Card
        StatisticsCard(statistics = statistics)

        // Quick Actions Section
        OperationButtonsSection(
            onAddRevenue = onAddRevenue,
            onAddExpense = onAddExpense,
            onTransfer = onTransfer
        )

        // Recent Activity Section
        RecentActivitySection(
            operations = recentOperations,
            isEmpty = recentOperations.isEmpty()
        )

        // Account Information Section
        AccountInformationSection(account = account)
    }
}

@Composable
fun AccountSummaryCard(
    account: Account,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = account.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Account Icon and Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(account.color, CircleShape)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Account",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
//                    Text(
//                        text = account.type.name,
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Balance
            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = account.balance.toBalanceString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (account.balance >= 0) {
                    Color(0xFF4CAF50) // Green for positive
                } else {
                    Color(0xFFF44336) // Red for negative
                }
            )

            if (account.isDebt) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Debt Account",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun OperationButtonsSection(
    onAddRevenue: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Revenue Button
            FilledTonalButton(
                onClick = onAddRevenue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Revenue",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add Revenue")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expense Button
            FilledTonalButton(
                onClick = onAddExpense,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = "Expense",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add Expense")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transfer Button
            FilledTonalButton(
                onClick = onTransfer,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Transfer",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Transfer Money")
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    accountId: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { /* Navigate to full operations list */ }) {
                    Text("View All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder for recent operations
            // You would fetch actual operations here
            Text(
                text = "No recent activity",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            // Example of how you might show operations when you have them:
            /*
            LazyColumn {
                items(operations) { operation ->
                    OperationListItem(operation = operation)
                }
            }
            */
        }
    }
}

@Composable
fun AccountInformationSection(
    account: Account,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Account Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            InformationRow(
                label = "Account ID",
                value = account.id.take(8) + "..." // Show shortened ID
            )

//            InformationRow(
//                label = "Account Type",
//                value = account.type.name
//            )

            InformationRow(
                label = "Created",
                value = account.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    .date.toString()
            )

            if (account.isDebt) {
                InformationRow(
                    label = "Status",
                    value = "Debt Account",
                    valueColor = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun InformationRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatisticsCard(
    statistics: AccountStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Last 30 Days",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Revenue
                StatisticItem(
                    label = "Revenue",
                    value = "${statistics.totalRevenue}",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                // Expenses
                StatisticItem(
                    label = "Expenses",
                    value = "${statistics.totalExpenses}",
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Net Change
                StatisticItem(
                    label = "Net Change",
                    value = "${statistics.netChange}",
                    color = if (statistics.netChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )

                // Transactions
                StatisticItem(
                    label = "Transactions",
                    value = statistics.transactionCount.toString(),
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecentActivitySection(
    operations: List<Operation>,
    isEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                if (operations.isNotEmpty()) {
                    TextButton(onClick = { /* Navigate to full operations list */ }) {
                        Text("View All")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEmpty) {
                Text(
                    text = "No recent activity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp) // Set a maximum height
                ) {
                    items(operations) { operation ->
                        OperationListItem(operation = operation)
                    }
                }

            }
        }
    }
}

@Composable
fun OperationListItem(
    operation: Operation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon and description
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val (icon, color) = when (operation.type) {
                    OperationType.REVENUE -> Icons.AutoMirrored.Filled.TrendingUp to Color(
                        0xFF4CAF50
                    )

                    OperationType.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown to Color(
                        0xFFF44336
                    )

                    OperationType.TRANSFER -> Icons.AutoMirrored.Filled.CallReceived to Color(
                        0xFF2196F3
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = operation.type.name,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = operation.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = operation.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Amount
            Text(
                text = "${operation.amount}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "No Account",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Account not found",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
