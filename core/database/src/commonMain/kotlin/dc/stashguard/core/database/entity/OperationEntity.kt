@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dc.stashguard.core.common.DateUtils
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey
    val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "account_id")
    val accountId: String,

    // OperationType name: "REVENUE", "EXPENSE" or "TRANSFER"
    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "date")
    val date: String, // ISO date format: "2024-01-15"

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = DateUtils.currentInstantMillis(),

    // For transfers - reference to the other account involved
    @ColumnInfo(name = "linked_operation_id")
    val linkedOperationId: String? = null,

    @ColumnInfo(name = "to_account_id")
    val toAccountId: String? = null
)
