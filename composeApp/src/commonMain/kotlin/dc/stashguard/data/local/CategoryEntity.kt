@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import dc.stashguard.util.DateUtils
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "categories")
@Serializable
data class CategoryEntity(
    @PrimaryKey
    val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: Int, // Store as Int (ARGB)

    @ColumnInfo(name = "icon_name")
    val iconName: String,

    @ColumnInfo(name = "type")
    val type: CategoryType,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = DateUtils.currentInstantMillis()
)

@Serializable
enum class CategoryType {
    REVENUE,
    EXPENSE,
    BOTH
}