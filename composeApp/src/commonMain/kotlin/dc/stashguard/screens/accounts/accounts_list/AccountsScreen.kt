@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package dc.stashguard.screens.accounts.accounts_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import dc.stashguard.model.Account
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.absoluteValue
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

private val logger = Logger.withTag("AccountsScreen")

private const val TOTAL_BALANCE_KEY = "total_balance"

@Composable
fun AccountsScreen(
    onNavigateToAccountDetails: (String) -> Unit,
    onNavigateToEditAccount: (String) -> Unit,
    onNavigateToAddAccount: () -> Unit,
    viewModel: AccountsViewModel = koinViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()

    // Local copy that follows the drag. It must stay one State object for the screen's lifetime:
    // the drag handle's pointerInput keeps the onDragStopped lambda from when it started, so a
    // recreated State would leave that lambda reading (and saving) a stale list.
    var orderedAccounts by remember { mutableStateOf(accounts) }
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIndex = orderedAccounts.indexOfFirst { it.id == from.key }
        val toIndex = orderedAccounts.indexOfFirst { it.id == to.key }
        if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyListState
        orderedAccounts = orderedAccounts.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
    // Follow database updates, but don't overwrite the order mid-drag
    LaunchedEffect(accounts) {
        if (!reorderableState.isAnyItemDragging) orderedAccounts = accounts
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Accounts List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            // Extra bottom space so the last card can scroll clear of the FAB
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (accounts.isNotEmpty()) {
                logger.d("Show accounts list: $orderedAccounts")

                // Total Balance Card (first item)
                val totalBalance = orderedAccounts.sumOf { it.balance }
                item(key = TOTAL_BALANCE_KEY) {
                    AccountCard(
                        Account(
                            name = "Balance",
                            balance = totalBalance,
                            color = Color.Black
                        ),
                        isTotal = true
                    )
                }

                // Accounts List, reorderable by long-press and drag
                items(orderedAccounts, key = { it.id }) { account ->
                    ReorderableItem(reorderableState, key = account.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                        AccountCard(
                            account = account,
                            onClick = { onNavigateToAccountDetails(account.id) },
                            onEdit = { onNavigateToEditAccount(account.id) },
                            modifier = Modifier
                                .longPressDraggableHandle(
                                    onDragStarted = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDragStopped = {
                                        viewModel.reorderAccounts(orderedAccounts.map { it.id })
                                    }
                                )
                                .shadow(elevation, RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddAccount,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add account"
            )
        }
    }
}

@Composable
fun AccountCard(
    account: Account,
    isTotal: Boolean = false,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val formattedBalance = formatCurrency(account.balance)
    val textColor = Color.White
//    val balanceColor = if (isDebt && balance < 0) Color.Red else textColor
    val balanceColor = textColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(account.color)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = account.name,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
            ) {
                Text(
                    text = formattedBalance,
                    color = balanceColor,
                    fontSize = 16.sp,
                    fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = !isTotal, onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTotal) Icons.Default.PieChart else Icons.Default.Edit,
                        contentDescription = if (isTotal) "Total Balance" else "Edit",
                        tint = textColor.copy(0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val sign = if (amount < 0) "-" else ""
    val absAmount = amount.absoluteValue
    return "$sign${absAmount.formatNumber()} ₽"
}

private fun Double.formatNumber(): String {
    return this.toInt().toString().replace("\\B(?=(\\d{3})+(?!\\d))".toRegex(), " ")
}