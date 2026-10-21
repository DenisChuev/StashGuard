@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)

package dc.stashguard.feature.accounts.details

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.core.common.formatForOperations
import dc.stashguard.core.common.toCurrencyString
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.ui.DEFAULT_CATEGORY_ICON
import dc.stashguard.core.ui.categoryIcon
import dc.stashguard.core.ui.color
import dc.stashguard.core.ui.icon
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

/** Space around the expanded account card; it shrinks to nothing as the card collapses. */
private val CARD_MARGIN = 16.dp

/** Height of the collapsed account card: a toolbar. */
private val COLLAPSED_HEADER_HEIGHT = 64.dp

@Composable
fun DetailsAccountScreen(
    modifier: Modifier = Modifier,
    accountId: String,
    onNavigateToEditAccount: (String) -> Unit,
    onNavigateAddOperation: (String, OperationType) -> Unit,
    onNavigateToEditOperation: (accountId: String, operationId: String, OperationType) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DetailsAccountViewModel = koinViewModel { parametersOf(accountId) }
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val recentOperations by viewModel.recentOperations.collectAsStateWithLifecycle()
    val dailyTotals by viewModel.dailyTotals.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accountNames by viewModel.accountNames.collectAsStateWithLifecycle()
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
                categories = categories,
                accountNames = accountNames,
                canLoadMoreOperations = canLoadMoreOperations,
                onLoadMoreOperations = viewModel::loadMoreOperations,
                onNavigateBack = onNavigateBack,
                onEditAccount = { onNavigateToEditAccount(accountId) },
                onAddRevenue = { onNavigateAddOperation(accountId, OperationType.REVENUE) },
                onAddExpense = { onNavigateAddOperation(accountId, OperationType.EXPENSE) },
                onTransfer = { onNavigateAddOperation(accountId, OperationType.TRANSFER) },
                onOperationClick = { onNavigateToEditOperation(accountId, it.id, it.type) },
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
    categories: Map<String, Category>,
    accountNames: Map<String, String>,
    canLoadMoreOperations: Boolean,
    onLoadMoreOperations: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    onAddRevenue: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    onOperationClick: (Operation) -> Unit,
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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        // A full-width bank card, plus its top margin
        val expandedHeaderHeight = CARD_MARGIN + (maxWidth - CARD_MARGIN * 2) / BANK_CARD_ASPECT_RATIO
        val collapseRangePx = with(density) { (expandedHeaderHeight - COLLAPSED_HEADER_HEIGHT).toPx() }
            .coerceAtLeast(0f)

        // How far the header has collapsed, in px, from 0 (card) to collapseRangePx (toolbar)
        var collapseOffsetPx by remember { mutableFloatStateOf(0f) }
        val collapseOffset = collapseOffsetPx.coerceIn(0f, collapseRangePx)
        val collapseFraction = if (collapseRangePx > 0f) collapseOffset / collapseRangePx else 0f

        // Scrolling up collapses the header before the list moves; scrolling down expands it
        // only once the list is back at its top
        val headerScrollConnection = remember(collapseRangePx) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y >= 0f) return Offset.Zero
                    val previous = collapseOffsetPx.coerceIn(0f, collapseRangePx)
                    collapseOffsetPx = (previous - available.y).coerceAtMost(collapseRangePx)
                    return Offset(0f, previous - collapseOffsetPx)
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    if (available.y <= 0f) return Offset.Zero
                    val previous = collapseOffsetPx.coerceIn(0f, collapseRangePx)
                    collapseOffsetPx = (previous - available.y).coerceAtLeast(0f)
                    return Offset(0f, previous - collapseOffsetPx)
                }

                // Never rest half-collapsed, where neither the card's content nor the toolbar's shows:
                // settle on the closer end once a scroll or fling is over
                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    val current = collapseOffsetPx.coerceIn(0f, collapseRangePx)
                    if (current > 0f && current < collapseRangePx) {
                        val target = if (current > collapseRangePx / 2) collapseRangePx else 0f
                        animate(initialValue = current, targetValue = target) { value, _ ->
                            collapseOffsetPx = value
                        }
                    }
                    return Velocity.Zero
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(headerScrollConnection)
        ) {
            AccountHeader(
                account = account,
                collapseFraction = collapseFraction,
                onNavigateBack = onNavigateBack,
                onEditAccount = onEditAccount,
                modifier = Modifier.height(with(density) { expandedHeaderHeight - collapseOffset.toDp() })
            )

            AccountDetailsList(
                listState = listState,
                operationsByDay = operationsByDay,
                isEmpty = recentOperations.isEmpty(),
                dailyTotals = dailyTotals,
                accountName = account.name,
                categories = categories,
                accountNames = accountNames,
                canLoadMoreOperations = canLoadMoreOperations,
                onAddRevenue = onAddRevenue,
                onAddExpense = onAddExpense,
                onTransfer = onTransfer,
                onOperationClick = onOperationClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AccountDetailsList(
    listState: LazyListState,
    operationsByDay: Map<LocalDate, List<Operation>>,
    isEmpty: Boolean,
    dailyTotals: Map<LocalDate, Double>,
    accountName: String,
    categories: Map<String, Category>,
    accountNames: Map<String, String>,
    canLoadMoreOperations: Boolean,
    onAddRevenue: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    onOperationClick: (Operation) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "quick_actions") {
            QuickActionsRow(
                onAddRevenue = onAddRevenue,
                onAddExpense = onAddExpense,
                onTransfer = onTransfer,
                modifier = Modifier.padding(horizontal = 8.dp)
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

        if (isEmpty) {
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
                    val category = categories[operation.category]
                    OperationListItem(
                        operation = operation,
                        title = {
                            if (operation.type == OperationType.TRANSFER) {
                                val (from, to) = transferAccounts(operation, accountName, accountNames)
                                TransferTitle(from = from, to = to)
                            } else {
                                // Expenses and revenues can be saved without a category
                                HistoryTitle(text = category?.name ?: "Uncategorized")
                            }
                        },
                        category = category,
                        onClick = { onOperationClick(operation) }
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

/**
 * The account as a full-width bank card that collapses into a toolbar, with the name as the
 * title and the balance as the subtitle. The caller sets the height as it collapses.
 */
@Composable
private fun AccountHeader(
    account: Account,
    collapseFraction: Float,
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val margin = lerp(CARD_MARGIN, 0.dp, collapseFraction)
    // The card's content fades out over the first half of the collapse, the toolbar's in over the second
    val cardContentAlpha = (1f - collapseFraction * 2f).coerceIn(0f, 1f)
    val toolbarContentAlpha = (collapseFraction * 2f - 1f).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = margin, top = margin, end = margin),
        shape = RoundedCornerShape(lerp(12.dp, 0.dp, collapseFraction)),
        // Solid account color with white content, like the account's card in the accounts list
        color = account.color,
        contentColor = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .alpha(cardContentAlpha)
                    )
                    // Toolbar title and subtitle
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .alpha(toolbarContentAlpha)
                    ) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = account.balance.toCurrencyString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalContentColor.current.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = onEditAccount) {
                    // The pencil fills more of its 24dp box than the back arrow; 20dp evens them out
                    Icon(Icons.Default.Edit, "Edit Account", modifier = Modifier.size(20.dp))
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // The balance would spill over the top row as the card gets shorter
                    .clipToBounds()
                    .alpha(cardContentAlpha)
                    .padding(horizontal = 16.dp)
            ) {
                // Shrinks a long balance to stay on one line
                Text(
                    text = account.balance.toCurrencyString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = 36.sp)
                )

                if (account.isDebt) {
                    Text(
                        text = "Debt Account",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.8f)
                    )
                }
            }
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
        horizontalArrangement = Arrangement.SpaceBetween,
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
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
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
    title: @Composable () -> Unit,
    category: Category?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Transfers use the Transfer quick action's icon, as the title shows the direction. Other operations
    // show their category, like the category pickers do, or the generic category icon if it's gone
    val transferColor = Color(0xFF2196F3)
    val (icon, background, tint) = when {
        operation.type == OperationType.TRANSFER -> Triple(
            Icons.Default.SwapHoriz,
            transferColor.copy(alpha = 0.15f),
            transferColor
        )

        else -> Triple(
            category?.icon ?: categoryIcon(DEFAULT_CATEGORY_ICON),
            category?.color ?: MaterialTheme.colorScheme.outline,
            Color.White
        )
    }
    val isMoneyIn = operation.isMoneyIn

    // Flat row: the day header above already shows the date. Tapping it opens the operation for editing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            title()
            // The amount's sign and color already tell money in from money out
            if (operation.note.isNotBlank()) {
                Text(
                    text = operation.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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

@Composable
private fun HistoryTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/** "From → To", with the arrow as an icon so it sits centered between the names, unlike the "→" character. */
@Composable
private fun TransferTitle(from: String, to: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Long names share the width rather than pushing the other one out
        HistoryTitle(text = from, modifier = Modifier.weight(1f, fill = false))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "to",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(16.dp)
        )
        HistoryTitle(text = to, modifier = Modifier.weight(1f, fill = false))
    }
}

/** Whether money came into this account or left it. */
private val Operation.isMoneyIn: Boolean
    get() = type == OperationType.REVENUE || (type == OperationType.TRANSFER && isIncoming)

private val Operation.signedAmount: Double
    get() = if (isMoneyIn) amount else -amount

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
            // A softer black than the operation names below it
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
