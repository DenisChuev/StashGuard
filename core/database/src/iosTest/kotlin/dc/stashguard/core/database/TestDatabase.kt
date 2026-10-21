package dc.stashguard.core.database

import androidx.room.Room
import dc.stashguard.core.database.entity.AccountEntity
import dc.stashguard.core.database.entity.CategoryEntity
import dc.stashguard.core.database.entity.OperationEntity

// DAO tests run on the iOS simulator only: on the Android JVM, Room needs a Context and the
// bundled SQLite driver has no native library for the host machine.

/** A fresh in-memory database with the app's configuration (bundled driver, IO dispatcher). */
fun inMemoryDatabase(): AppDatabase = getAppDatabase(Room.inMemoryDatabaseBuilder<AppDatabase>())

fun accountEntity(id: String, name: String = id, balance: Double = 0.0, position: Int = 0) =
    AccountEntity(id = id, name = name, balance = balance, color = 0, position = position, createdAt = 0)

fun operationEntity(
    id: String,
    accountId: String,
    type: String,
    amount: Double,
    date: String = "2026-10-01",
    createdAt: Long = 0,
    category: String = "category_food",
    linkedOperationId: String? = null,
    toAccountId: String? = null,
    isIncoming: Boolean = false,
) = OperationEntity(
    id = id,
    accountId = accountId,
    type = type,
    amount = amount,
    category = category,
    date = date,
    note = "",
    createdAt = createdAt,
    linkedOperationId = linkedOperationId,
    toAccountId = toAccountId,
    isIncoming = isIncoming,
)

fun categoryEntity(id: String, name: String = id, type: String = "EXPENSE") =
    CategoryEntity(id = id, name = name, color = 0, iconName = "category", type = type, createdAt = 0)
