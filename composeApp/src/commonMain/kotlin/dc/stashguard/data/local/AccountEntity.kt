@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package dc.stashguard.data.local

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import dc.stashguard.util.DateUtils
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val name: String,
    val balance: Double,
    val color: Int,
    val isDebt: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = DateUtils.currentDateMillis(),
)

object RoomConverters {
    @TypeConverter
    fun colorToInt(color: Color): Int {
        return color.value.toInt() // Convert Compose Color to ARGB Int
    }

    @TypeConverter
    fun intToColor(argbInt: Int): Color {
        return Color(argbInt.toULong()) // Convert ARGB Int back to Compose Color
    }
}