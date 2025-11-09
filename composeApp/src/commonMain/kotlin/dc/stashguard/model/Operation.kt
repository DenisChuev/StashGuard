@file:OptIn(ExperimentalTime::class)

package dc.stashguard.model

import dc.stashguard.data.local.OperationEntity
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
enum class OperationType {
    REVENUE,
    EXPENSE,
    TRANSFER
}

@Serializable
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
    val toAccountId: String? = null
)

fun OperationEntity.toOperation(): Operation {
    return Operation(
        id = this.id,
        accountId = this.accountId,
        type = this.type,
        amount = this.amount,
        category = this.category,
        date = LocalDate.parse(this.date),
        note = this.note,
        createdAt = Instant.fromEpochMilliseconds(this.createdAt),
        linkedOperationId = this.linkedOperationId,
        toAccountId = this.toAccountId
    )
}

fun Operation.toOperationEntity(): OperationEntity {
    return OperationEntity(
        id = this.id,
        accountId = this.accountId,
        type = this.type,
        amount = this.amount,
        category = this.category,
        date = this.date.toString(),
        note = this.note,
        createdAt = this.createdAt.toEpochMilliseconds(),
        linkedOperationId = this.linkedOperationId,
        toAccountId = this.toAccountId
    )
}