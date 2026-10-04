@file:OptIn(ExperimentalTime::class)

package dc.stashguard.core.data.mapper

import dc.stashguard.core.database.entity.OperationEntity
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// created_at is stored as epoch milliseconds, date as an ISO string ("2024-01-15").
internal fun OperationEntity.toDomain(): Operation {
    return Operation(
        id = this.id,
        accountId = this.accountId,
        type = OperationType.valueOf(this.type),
        amount = this.amount,
        category = this.category,
        date = LocalDate.parse(this.date),
        note = this.note,
        createdAt = Instant.fromEpochMilliseconds(this.createdAt),
        linkedOperationId = this.linkedOperationId,
        toAccountId = this.toAccountId
    )
}

internal fun Operation.toEntity(): OperationEntity {
    return OperationEntity(
        id = this.id,
        accountId = this.accountId,
        type = this.type.name,
        amount = this.amount,
        category = this.category,
        date = this.date.toString(),
        note = this.note,
        createdAt = this.createdAt.toEpochMilliseconds(),
        linkedOperationId = this.linkedOperationId,
        toAccountId = this.toAccountId
    )
}
