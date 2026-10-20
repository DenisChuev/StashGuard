@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)

package dc.stashguard.feature.accounts.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import dc.stashguard.core.common.formatForOperations
import dc.stashguard.core.common.toCurrencyString
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.ui.color
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

/** How many items from the end of the list the next batch of operations starts loading. */
private const val LOAD_MORE_THRESHOLD = 5

/** Width to height of a bank card (ISO/IEC 7810 ID-1, 85.60 x 53.98 mm). */
private const val BANK_CARD_ASPECT_RATIO = 85.60f / 53.98f

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
    val dailyTotals by viewModel.dailyTotals.collectAsStateWithLifecycle()
    val categoryNames by viewModel.categoryNames.collectAsStateWithLifecycle()
    val canLoadMoreOperations by viewModel.canLoadMoreOperations.collectAsStateWithLifecycle()

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
                dailyTotals = dailyTotals,
                categoryNames = categoryNames,
                canLoadMoreOperations = canLoadMoreOperations,
                onLoadMoreOperations = viewModel::loadMoreOperations,
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
    dailyTotals: Map<LocalDate, Double>,
    categoryNames: Map<String, String>,
    canLoadMoreOperations: Boolean,
    onLoadMoreOperations: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    onAddRevenue: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val operationsByDay = remember(recentOperations) { recentOperations.groupBy { it.date } }
    val listState = rememberLazyListState()
    val currentOnLoadMoreOperations by rememberUpdatedState(onLoadMoreOperations)

    // Load the next batch once the end of the list scrolls close into view
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { currentOnLoadMoreOperations() }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Account Summary Card (also the screen's toolbar)
        item(key = "summary") {
            AccountSummaryCard(
                account = account,
                onNavigateBack = onNavigateBack,
                onEditAccount = onEditAccount
            )
        }

        item(key = "quick_actions") {
            QuickActionsRow(
                onAddRevenue = onAddRevenue,
                onAddExpense = onAddExpense,
                onTransfer = onTransfer
            )
        }

        item(key = "history_header") {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (recentOperations.isEmpty()) {
            item(key = "history_empty") {
                Text(
                    text = "No operations yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Operations come newest first, so grouping keeps the days in order
            operationsByDay.forEach { (date, operations) ->
                item(key = "day_$date") {
                    HistoryDayHeader(
                        date = date,
                        // Falls back to the loaded operations until the totals query emits
                        total = dailyTotals[date] ?: operations.sumOf { it.signedAmount }
                    )
                }
                items(operations, key = { it.id }) { operation ->
                    OperationListItem(
                        operation = operation,
                        // Falls back to the stored id if the category is gone
                        categoryName = categoryNames[operation.category] ?: operation.category
                    )
                }
            }
        }

        if (canLoadMoreOperations) {
            item(key = "loading_more") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun AccountSummaryCard(
    account: Account,
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(BANK_CARD_ASPECT_RATIO),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        // Solid account color with white content, like the account's card in the accounts list
        colors = CardDefaults.cardColors(
            containerColor = account.color,
            contentColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Back, centered name and edit, in place of a toolbar
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditAccount) {
                    Icon(Icons.Default.Edit, "Edit Account")
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
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

            // Mirrors the 48dp top row, so the balance sits at the card's center
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAddRevenue: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier.fillMaxWidth()
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
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
    val isMoneyIn = operation.isMoneyIn

    // Flat row: the day header above already shows the date
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = operation.type.name,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = operation.note.ifBlank { operation.type.label },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Money in stands out in green, money out stays neutral
        Text(
            text = (if (isMoneyIn) "+" else "-") + operation.amount.toCurrencyString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (isMoneyIn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Whether money came into this account or left it. */
private val Operation.isMoneyIn: Boolean
    get() = type == OperationType.REVENUE || (type == OperationType.TRANSFER && isIncoming)

private val Operation.signedAmount: Double
    get() = if (isMoneyIn) amount else -amount

private val OperationType.label: String
    get() = when (this) {
        OperationType.REVENUE -> "Revenue"
        OperationType.EXPENSE -> "Expense"
        OperationType.TRANSFER -> "Transfer"
    }

/** Date on the left, the day's net change in the top right corner. */
@Composable
private fun HistoryDayHeader(
    date: LocalDate,
    total: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = date.formatForOperations(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = (if (total > 0) "+" else "") + total.toCurrencyString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
