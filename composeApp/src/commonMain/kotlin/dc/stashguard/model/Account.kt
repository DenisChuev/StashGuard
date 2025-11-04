@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.model

import androidx.compose.ui.graphics.Color
import dc.stashguard.data.local.AccountEntity
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Account(
    val id: String = Uuid.random().toString(),
    val name: String,
    val balance: Double,
    val color: Color,
    val isDebt: Boolean = false
)

fun Account.toAccountEntity(): AccountEntity {
    return AccountEntity(
        id = this.id,
        name = this.name,
        balance = this.balance,
        colorArgb = this.color.value.toInt(),
        isDebt = this.isDebt
    )
}

fun AccountEntity.toAccount(): Account {
    return Account(
        id = this.id,
        name = this.name,
        balance = this.balance,
        color = Color(this.colorArgb.toULong()),
        isDebt = this.isDebt
    )
}