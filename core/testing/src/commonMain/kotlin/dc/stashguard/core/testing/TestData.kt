@file:OptIn(ExperimentalTime::class)

package dc.stashguard.core.testing

import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

val TEST_DATE = LocalDate(2026, 10, 1)

fun account(id: String, balance: Double = 0.0, position: Int = 0) =
    Account(id = id, name = id, balance = balance, colorArgb = 0, position = position)

fun category(id: String, type: CategoryType = CategoryType.EXPENSE) =
    Category(id = id, name = id, colorArgb = 0, iconName = "category", type = type)

fun operation(
    id: String,
    accountId: String,
    type: OperationType,
    amount: Double,
    category: String = "category_food",
) = Operation(
    id = id,
    accountId = accountId,
    type = type,
    amount = amount,
    category = category,
    date = TEST_DATE,
    note = "",
    createdAt = Instant.fromEpochMilliseconds(0),
)

/** Both sides of a transfer of [amount] from [fromAccountId] to [toAccountId]: (sending, receiving). */
fun transfer(
    fromAccountId: String,
    toAccountId: String,
    amount: Double,
    linkedOperationId: String = "transfer",
): Pair<Operation, Operation> {
    val sending = operation("$linkedOperationId-out", fromAccountId, OperationType.TRANSFER, amount, TRANSFER_CATEGORY_ID)
        .copy(linkedOperationId = linkedOperationId, toAccountId = toAccountId, isIncoming = false)
    val receiving = operation("$linkedOperationId-in", toAccountId, OperationType.TRANSFER, amount, TRANSFER_CATEGORY_ID)
        .copy(linkedOperationId = linkedOperationId, toAccountId = fromAccountId, isIncoming = true)
    return sending to receiving
}
