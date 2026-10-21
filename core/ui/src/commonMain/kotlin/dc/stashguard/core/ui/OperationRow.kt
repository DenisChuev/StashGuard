package dc.stashguard.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dc.stashguard.core.common.formatForOperations
import dc.stashguard.core.common.toCurrencyString
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import kotlinx.datetime.LocalDate

private val TransferColor = Color(0xFF2196F3)
private val MoneyInColor = Color(0xFF4CAF50)

/** Whether money came into the operation's account or left it. */
val Operation.isMoneyIn: Boolean
    get() = type == OperationType.REVENUE || (type == OperationType.TRANSFER && isIncoming)

/**
 * A transfer's account names as (from, to). Either account may have been deleted, so it isn't
 * always in [accountNames].
 */
fun transferAccounts(operation: Operation, accountNames: Map<String, String>): Pair<String, String> {
    val ownName = accountNames[operation.accountId] ?: "Deleted account"
    val otherName = operation.toAccountId?.let { accountNames[it] } ?: "Deleted account"
    return if (operation.isIncoming) otherName to ownName else ownName to otherName
}

/**
 * One operation in a history list: its category's icon and name, or for a transfer the Transfer
 * icon and "From → To". [category] is null for an operation saved without one. With
 * [signTransfers] off, as in a list across all accounts, a transfer's amount has no sign, since it
 * only moves money between the user's own accounts.
 */
@Composable
fun OperationRow(
    operation: Operation,
    category: Category?,
    accountNames: Map<String, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    signTransfers: Boolean = true
) {
    val isTransfer = operation.type == OperationType.TRANSFER
    // Transfers use the Transfer quick action's icon, as the title shows the direction. Other operations
    // show their category, like the category pickers do, or the generic category icon if there is none
    val (icon, background, tint) = if (isTransfer) {
        Triple(Icons.Default.SwapHoriz, TransferColor.copy(alpha = 0.15f), TransferColor)
    } else {
        Triple(
            category?.icon ?: categoryIcon(DEFAULT_CATEGORY_ICON),
            category?.color ?: MaterialTheme.colorScheme.outline,
            Color.White
        )
    }

    // Flat row: the day header above already shows the date
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
            if (isTransfer) {
                val (from, to) = transferAccounts(operation, accountNames)
                TransferTitle(from = from, to = to)
            } else {
                // Expenses and revenues can be saved without a category
                RowTitle(text = category?.name ?: "Uncategorized")
            }
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
        val sign = when {
            isTransfer && !signTransfers -> ""
            operation.isMoneyIn -> "+"
            else -> "-"
        }
        Text(
            text = sign + operation.amount.toCurrencyString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (sign == "+") MoneyInColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RowTitle(text: String, modifier: Modifier = Modifier) {
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
        RowTitle(text = from, modifier = Modifier.weight(1f, fill = false))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "to",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(16.dp)
        )
        RowTitle(text = to, modifier = Modifier.weight(1f, fill = false))
    }
}

/** Date on the left, the day's net change in the top right corner. */
@Composable
fun OperationDayHeader(
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
