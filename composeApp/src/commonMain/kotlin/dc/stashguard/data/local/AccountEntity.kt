package dc.stashguard.data.local

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val name: String,
    val balance: Double,
    val colorArgb: Int,
    val isDebt: Boolean = false
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