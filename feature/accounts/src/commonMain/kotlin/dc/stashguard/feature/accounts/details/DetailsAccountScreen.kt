@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)

package dc.stashguard.feature.accounts.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.core.common.toCurrencyString
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.AccountStatistics
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.ui.color
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
    val categoryNames by viewModel.categoryNames.collectAsStateWithLifecycle()
    val statistics by viewModel.accountStatistics.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // With an account loaded, the account card acts as the toolbar
            if (account == null) {
                DetailsTopAppBar(
                    onNavigateBack = onNavigateBack,
                    onEditAccount = { onNavigateToEditAccount(accountId) },
                    isLoading = isLoading,
                    account = null
                )
            }
        },
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
                categoryNames = categoryNames,
                statistics = statistics,
                onNavigateBack = onNavigateBack,
                onEditAccount = { onNavigateToEditAccount(accountId) },
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
    categoryNames: Map<String, String>,
    statistics: AccountStatistics,
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    onAddRevenue: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Account Summary Card (also the screen's toolbar)
        AccountSummaryCard(
            account = account,
            onNavigateBack = onNavigateBack,
            onEditAccount = onEditAccount,
            onAddRevenue = onAddRevenue,
            onAddExpense = onAddExpense,
            onTransfer = onTransfer
        )

        // Statistics Card
        StatisticsCard(statistics = statistics)

        // Recent Activity Section
        RecentActivitySection(
            operations = recentOperations,
            categoryNames = categoryNames,
            isEmpty = recentOperations.isEmpty()
        )
    }
}

@Composable
fun AccountSummaryCard(
    account: Account,
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    onAddRevenue: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        // Solid account color with white content, like the account's card in the accounts list
        colors = CardDefaults.cardColors(
            containerColor = account.color,
            contentColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 16.dp)
        ) {
            // Back, name and edit, in place of a toolbar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditAccount) {
                    Icon(Icons.Default.Edit, "Edit Account")
                }
            }

            // Balance, aligned with the name (past the 48dp back button)
            Column(modifier = Modifier.padding(start = 48.dp, end = 16.dp)) {
                Text(
                    text = account.balance.toCurrencyString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                if (account.isDebt) {
                    Text(
                        text = "Debt Account",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick actions
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Revenue",
                    color = Color(0xFF4CAF50),
                    onClick = onAddRevenue
                )
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    label = "Expense",
                    color = Color(0xFFF44336),
                    onClick = onAddExpense
                )
                QuickActionButton(
                    icon = Icons.Default.SwapHoriz,
                    label = "Transfer",
                    color = Color(0xFF2196F3),
                    onClick = onTransfer
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // White circle with a colored icon stays readable on any account color
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // The label below describes the button
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        // Lighter than the default card grey
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
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
                    value = statistics.totalRevenue.toCurrencyString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                // Expenses
                StatisticItem(
                    label = "Expenses",
                    value = statistics.totalExpenses.toCurrencyString(),
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
                    value = statistics.netChange.toCurrencyString(),
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
    categoryNames: Map<String, String>,
    isEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        // Lighter than the default card grey
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
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
                        OperationListItem(
                            operation = operation,
                            // Falls back to the stored id if the category is gone
                            categoryName = categoryNames[operation.category] ?: operation.category
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun OperationListItem(
    operation: Operation,
    categoryName: String,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (operation.type) {
        OperationType.REVENUE -> Icons.AutoMirrored.Filled.TrendingUp to Color(0xFF4CAF50)
        OperationType.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown to Color(0xFFF44336)
        OperationType.TRANSFER -> if (operation.isIncoming) {
            Icons.AutoMirrored.Filled.CallReceived to Color(0xFF2196F3)
        } else {
            Icons.AutoMirrored.Filled.CallMade to Color(0xFF2196F3)
        }
    }
    // Sign by whether money came into this account or left it
    val isMoneyIn = operation.type == OperationType.REVENUE ||
        (operation.type == OperationType.TRANSFER && operation.isIncoming)

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
                Icon(
                    imageVector = icon,
                    contentDescription = operation.type.name,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = categoryName,
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
                text = (if (isMoneyIn) "+" else "-") + operation.amount.toCurrencyString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
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
