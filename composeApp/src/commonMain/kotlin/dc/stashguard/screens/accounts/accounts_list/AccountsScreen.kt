@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.screens.accounts.accounts_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import dc.stashguard.model.Account
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue
import kotlin.uuid.ExperimentalUuidApi

private val logger = Logger.withTag("AccountsScreen")

@Composable
fun AccountsScreen(
    modifier: Modifier = Modifier,
    onNavigateToOperations: () -> Unit,
    onNavigateToAccountDetails: (String) -> Unit,
    onNavigateToAddAccount: () -> Unit,
    viewModel: AccountsViewModel = koinViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        // Accounts List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (accounts.isNotEmpty()) {
                logger.d("Show accounts list: $accounts")

                // Total Balance Card (first item)
                val totalBalance = accounts.sumOf { it.balance }
                item {
                    AccountCard(
                        Account(
                            name = "Balance",
                            balance = totalBalance,
                            color = Color.Black
                        ),
                        isTotal = true
                    )
                }

                // Accounts List
                items(accounts) { account ->
                    AccountCard(
                        account = account,
                        onClick = { onNavigateToOperations() },
                        onEdit = { onNavigateToAccountDetails(account.id) }
                    )
                }
            }
        }

        // Add Account Card at the bottom
        AddAccountCard {
            onNavigateToAddAccount()
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    isTotal: Boolean = false,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    val formattedBalance = formatCurrency(account.balance)
    val textColor = Color.White
//    val balanceColor = if (isDebt && balance < 0) Color.Red else textColor
    val balanceColor = textColor

    Box(
        modifier = Modifier
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

@Composable
private fun AddAccountCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add account",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
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