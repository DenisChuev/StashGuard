@file:OptIn(ExperimentalTime::class)

package dc.stashguard.core.data.mapper

import dc.stashguard.core.database.entity.AccountEntity
import dc.stashguard.core.domain.model.Account
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// created_at is stored as epoch seconds.
internal fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = this.id,
        name = this.name,
        balance = this.balance,
        color = this.colorArgb,
        isDebt = this.isDebt,
        createdAt = this.createdAt.epochSeconds,
        position = this.position
    )
}

internal fun AccountEntity.toDomain(): Account {
    return Account(
        id = this.id,
        name = this.name,
        balance = this.balance,
        colorArgb = this.color,
        isDebt = this.isDebt,
        createdAt = Instant.fromEpochSeconds(this.createdAt),
        position = this.position
    )
}
