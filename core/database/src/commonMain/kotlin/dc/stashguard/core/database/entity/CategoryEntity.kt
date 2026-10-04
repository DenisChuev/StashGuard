@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dc.stashguard.core.common.DateUtils
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: Int, // Store as Int (ARGB)

    @ColumnInfo(name = "icon_name")
    val iconName: String,

    // CategoryType name: "REVENUE", "EXPENSE" or "BOTH"
    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = DateUtils.currentInstantMillis()
)
