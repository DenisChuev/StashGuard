@file:OptIn(ExperimentalTime::class)

package dc.stashguard.core.domain.model

import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class OperationType {
    REVENUE,
    EXPENSE,
    TRANSFER
}

/**
 * A transfer is stored as two operations, one per account, sharing a [linkedOperationId].
 * On each side [toAccountId] is the other account, and [isIncoming] tells the receiving
 * side (true) from the sending side (false).
 */
data class Operation(
    val id: String,
    val accountId: String,
    val type: OperationType,
    val amount: Double,
    val category: String,
    val date: LocalDate,
    val note: String,
    val createdAt: Instant,
    val linkedOperationId: String? = null,
    val toAccountId: String? = null,
    val isIncoming: Boolean = false
)

/** Categories that can be picked for an operation of this type ([CategoryType.BOTH] always matches). */
val OperationType.categoryType: CategoryType
    get() = when (this) {
        OperationType.REVENUE -> CategoryType.REVENUE
        OperationType.EXPENSE -> CategoryType.EXPENSE
        OperationType.TRANSFER -> CategoryType.BOTH
    }
