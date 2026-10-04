package dc.stashguard.core.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dc.stashguard.core.database.dao.AccountDao
import dc.stashguard.core.database.dao.CategoryDao
import dc.stashguard.core.database.dao.OperationDao
import dc.stashguard.core.database.entity.AccountEntity
import dc.stashguard.core.database.entity.CategoryEntity
import dc.stashguard.core.database.entity.OperationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        AccountEntity::class,
        OperationEntity::class,
        CategoryEntity::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 3, to = 4), // accounts.position
    ]
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getAccountDao(): AccountDao
    abstract fun getOperationDao(): OperationDao
    abstract fun getCategoryDao(): CategoryDao
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
        .fallbackToDestructiveMigration(true)
        .build()
}