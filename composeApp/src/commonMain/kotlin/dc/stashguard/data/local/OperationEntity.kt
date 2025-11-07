@file:OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)

package dc.stashguard.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dc.stashguard.model.OperationType
import dc.stashguard.util.DateUtils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey
    val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "account_id")
    val accountId: String,

    @ColumnInfo(name = "type")
    val type: OperationType,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "date")
    val date: String, // ISO date format: "2024-01-15"

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = DateUtils.currentDateMillis(),

    // For transfers - reference to the other account involved
    @ColumnInfo(name = "linked_operation_id")
    val linkedOperationId: String? = null,

    @ColumnInfo(name = "to_account_id")
    val toAccountId: String? = null
)