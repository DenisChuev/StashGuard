package dc.stashguard.core.data.repository

import androidx.room.Room
import dc.stashguard.core.database.AppDatabase
import dc.stashguard.core.database.entity.AccountEntity
import dc.stashguard.core.database.getAppDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

// Runs on the iOS simulator only, like the DAO tests in :core:database
class RoomTransactionRunnerTest {
    private val database: AppDatabase = getAppDatabase(Room.inMemoryDatabaseBuilder<AppDatabase>())
    private val accountRepository = AccountRepositoryImpl(database.getAccountDao())
    private val transaction = RoomTransactionRunner(database)

    @AfterTest
    fun closeDatabase() = database.close()

    private suspend fun insertAccount(id: String, balance: Double) =
        database.getAccountDao().insertAccount(
            AccountEntity(id = id, name = id, balance = balance, color = 0, createdAt = 0)
        )

    @Test
    fun writesInsideTheTransactionAreSavedTogether() = runTest {
        insertAccount("a", 100.0)
        insertAccount("b", 0.0)

        transaction {
            accountRepository.adjustBalance("a", -40.0)
            accountRepository.adjustBalance("b", 40.0)
        }

        assertEquals(60.0, accountRepository.getAccount("a")?.balance)
        assertEquals(40.0, accountRepository.getAccount("b")?.balance)
    }

    // The use case tests rely on this: a failing use case leaves no partial writes
    @Test
    fun aFailureRollsBackEveryWriteInTheTransaction() = runTest {
        insertAccount("a", 100.0)

        assertFailsWith<IllegalStateException> {
            transaction {
                accountRepository.adjustBalance("a", -40.0)
                insertAccount("b", 0.0)
                error("Failed halfway")
            }
        }

        assertEquals(100.0, accountRepository.getAccount("a")?.balance)
        assertNull(accountRepository.getAccount("b"))
    }

    @Test
    fun adjustingAMissingAccountDoesNothing() = runTest {
        accountRepository.adjustBalance("missing", 10.0)

        assertNull(accountRepository.getAccount("missing"))
    }
}
