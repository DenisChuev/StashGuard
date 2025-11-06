@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package dc.stashguard.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dc.stashguard.data.local.AccountEntity
import dc.stashguard.util.DateUtils
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Serializable
enum class AccountType {
    SAVINGS,
    CHECKING,
    CREDIT_CARD,
    INVESTMENT
}

data class Account(
    val id: String = Uuid.random().toString(),
    val name: String,
    val balance: Double,
    val color: Color,
    val isDebt: Boolean = false,
    val createdAt: Instant = DateUtils.currentInstant(),
)

fun Account.toAccountEntity(): AccountEntity {
    return AccountEntity(
        id = this.id,
        name = this.name,
        balance = this.balance,
        colorArgb = this.color.toArgb(),
        isDebt = this.isDebt,
        createdAt = this.createdAt.epochSeconds
    )
}

fun AccountEntity.toAccount(): Account {
    return Account(
        id = this.id,
        name = this.name,
        balance = this.balance,
        color = Color(this.colorArgb),
        isDebt = this.isDebt,
        createdAt = Instant.fromEpochSeconds(this.createdAt)
    )
}