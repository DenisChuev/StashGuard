@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.uuid.ExperimentalUuidApi

@Database(
    entities = [
        AccountEntity::class,
        OperationEntity::class,
        CategoryEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getAccountDao(): AccountDao
    abstract fun getOperationDao(): OperationDao
    abstract fun getCategoryDao(): CategoryDao

//    companion object {
//        val MIGRATION_2_3 = object : Migration(2, 3) {
//            override fun migrate(connection: SQLiteConnection) {
//                connection.execSQL(
//                    """
//            CREATE TABLE categories (
//                id TEXT PRIMARY KEY NOT NULL,
//                name TEXT NOT NULL,
//                color INTEGER NOT NULL,
//                icon_name TEXT NOT NULL,
//                type TEXT NOT NULL,
//                created_at INTEGER NOT NULL
//            )
//            """
//                )
//
//                // Add category_id column to operations table
//                connection.execSQL(
//                    "ALTER TABLE operations ADD COLUMN category_id TEXT NOT NULL DEFAULT ''"
//                )
//
//                // Insert default categories
//                createDefaultCategories(connection)
//            }
//
//            private fun createDefaultCategories(database: SQLiteConnection) {
//                val defaultCategories = listOf(
//                    // Expense Categories
//                    "('${Uuid.random()}', 'Food', ${Color(0xFFF44336).value.toLong()}, 'restaurant', 'EXPENSE', ${DateUtils.currentDateMillis()})",
//                    "('${Uuid.random()}', 'Transport', ${Color(0xFF2196F3).value.toLong()}, 'directions_car', 'EXPENSE', ${DateUtils.currentDateMillis()})",
//                    "('${Uuid.random()}', 'Shopping', ${Color(0xFF9C27B0).value.toLong()}, 'shopping_cart', 'EXPENSE', ${DateUtils.currentDateMillis()})",
//                    "('${Uuid.random()}', 'Entertainment', ${Color(0xFFFF9800).value.toLong()}, 'movie', 'EXPENSE', ${DateUtils.currentDateMillis()})",
//                    "('${Uuid.random()}', 'Healthcare', ${Color(0xFF4CAF50).value.toLong()}, 'local_hospital', 'EXPENSE', ${DateUtils.currentDateMillis()})",
//                    "('${Uuid.random()}', 'Bills', ${Color(0xFF607D8B).value.toLong()}, 'receipt', 'EXPENSE', ${DateUtils.currentDateMillis()})",
//                    "('${Uuid.random()}', 'Education', ${Color(0xFF795548).value.toLong()}, 'school', 'EXPENSE', ${DateUtils.currentDateMillis()})",
//
//                    // Revenue Categories
//                    "('${Uuid.random()}', 'Salary', ${Color(0xFF4CAF50).value.toLong()}, 'work', 'REVENUE', ${DateUtils.currentDateMillis()})",
//                    "('${Uuid.random()}', 'Freelance', ${Color(0xFF2196F3).value.toLong()}, 'computer', 'REVENUE', ${DateUtils.currentDateMillis()})",
//                    "('${Uuid.random()}', 'Investment', ${Color(0xFFFFC107).value.toLong()}, 'trending_up', 'REVENUE', ${DateUtils.currentDateMillis()})",
//                    "('${Uuid.random()}', 'Gift', ${Color(0xFFE91E63).value.toLong()}, 'card_giftcard', 'REVENUE', ${DateUtils.currentDateMillis()})",
//
//                    // Both Categories
//                    "('${Uuid.random()}', 'Transfer', ${Color(0xFF9E9E9E).value.toLong()}, 'swap_horiz', 'BOTH', ${DateUtils.currentDateMillis()})"
//                )
//
//                defaultCategories.forEach { categoryValues ->
//                    database.execSQL("INSERT INTO categories VALUES $categoryValues")
//                }
//            }
//        }
//    }
//}
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
//        .addMigrations(MIGRATION_2_3)
        .fallbackToDestructiveMigration(true)
        .build()
}