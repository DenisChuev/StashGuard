@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class Account(
    val id: String = Uuid.random().toString(),
    val name: String,
    val balance: Double,
    val color: Color,
    val isDebt: Boolean = false
)

val accounts = listOf(
    Account(name = "Яндекс Pay", balance = 26297.0, color = Color(0xFFFF4081)),
    Account(name = "МТС деньги", balance = 9136.5, color = Color(0xFFE91E63)),
    Account(name = "Платина 100k", balance = -10000.0, color = Color(0xFFFFC107), isDebt = true),
    Account(name = "ВТБ", balance = 0.0, color = Color(0xFF2196F3)),
    Account(name = "Wallet", balance = 1325.0, color = Color(0xFF64B5F6)),
    Account(name = "ВТБ копилка", balance = 50268.0, color = Color(0xFF2196F3)),
)

@Composable
fun AccountsScreen(
    modifier: Modifier = Modifier,
    onNavigateToOperations: () -> Unit,
    onNavigateToAccountDetails: (Account) -> Unit,
    onNavigateToAddAccount: () -> Unit
) {
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
                // Total Balance Card (first item)
                val totalBalance = accounts.sumOf { it.balance }
                item {
                    AccountCard(
                        name = "Balance",
                        balance = totalBalance,
                        color = Color.Black,
                        isTotal = true
                    )
                }

                // Accounts List
                items(accounts) { account ->
                    AccountCard(
                        name = account.name,
                        balance = account.balance,
                        color = account.color,
                        isDebt = account.isDebt,
                        onClick = { onNavigateToOperations() },
                        onEdit = { onNavigateToAccountDetails(account) }
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
    name: String,
    balance: Double,
    color: Color,
    isDebt: Boolean = false,
    isTotal: Boolean = false,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    val formattedBalance = formatCurrency(balance)
    val textColor = Color.White
    val balanceColor = if (isDebt && balance < 0) Color.Red else textColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color)
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
                text = name,
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