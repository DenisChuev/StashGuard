package dc.stashguard.core.database

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccountDaoTest {
    private val database = inMemoryDatabase()
    private val dao = database.getAccountDao()

    @AfterTest
    fun closeDatabase() = database.close()

    @Test
    fun accountsAreOrderedByPositionThenByNameIgnoringCase() = runTest {
        dao.insertAccounts(
            listOf(
                accountEntity("1", name = "wallet", position = 1),
                accountEntity("2", name = "Cash", position = 1),
                accountEntity("3", name = "Savings", position = 0),
            )
        )

        assertEquals(listOf("Savings", "Cash", "wallet"), dao.getAllAccounts().first().map { it.name })
    }

    @Test
    fun maxPositionIsMinusOneWithoutAccounts() = runTest {
        assertEquals(-1, dao.getMaxPosition())

        dao.insertAccounts(listOf(accountEntity("1", position = 0), accountEntity("2", position = 4)))

        assertEquals(4, dao.getMaxPosition())
    }

    @Test
    fun updatePositionsStoresTheNewOrder() = runTest {
        dao.insertAccounts(
            listOf(
                accountEntity("a", position = 0),
                accountEntity("b", position = 1),
                accountEntity("c", position = 2),
            )
        )

        dao.updatePositions(listOf("c", "a", "b"))

        assertEquals(listOf("c", "a", "b"), dao.getAllAccounts().first().map { it.id })
    }

    @Test
    fun deletedAccountIsGone() = runTest {
        dao.insertAccount(accountEntity("a"))

        dao.deleteAccountById("a")

        assertNull(dao.getAccountByIdOnce("a"))
        assertNull(dao.getAccountById("a").first())
    }
}
