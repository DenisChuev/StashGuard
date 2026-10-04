@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package dc.stashguard.core.domain.model

import dc.stashguard.core.common.DateUtils
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


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
    val colorArgb: Int,
    val isDebt: Boolean = false,
    val createdAt: Instant = DateUtils.currentInstant(),
    val position: Int = 0,
)
