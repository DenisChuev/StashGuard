@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dc.stashguard.core.common.DateUtils
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val name: String,
    val balance: Double,
    val color: Int, // ARGB
    val isDebt: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = DateUtils.currentInstantMillis(),
    // User-defined order in the accounts list (drag and drop); lower comes first.
    @ColumnInfo(defaultValue = "0")
    val position: Int = 0,
)
