package dc.stashguard.core.database

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class OperationDaoTest {
    private val database = inMemoryDatabase()
    private val dao = database.getOperationDao()

    @AfterTest
    fun closeDatabase() = database.close()

    @Test
    fun accountOperationsAreNewestFirstAndLimited() = runTest {
        dao.insertOperations(
            listOf(
                operationEntity("old", "a", "EXPENSE", 1.0, date = "2026-09-30"),
                operationEntity("new-early", "a", "EXPENSE", 1.0, date = "2026-10-02", createdAt = 1),
                operationEntity("new-late", "a", "EXPENSE", 1.0, date = "2026-10-02", createdAt = 2),
                operationEntity("other-account", "b", "EXPENSE", 1.0, date = "2026-10-03"),
            )
        )

        assertEquals(listOf("new-late", "new-early", "old"), dao.getOperationsByAccount("a", 10).first().map { it.id })
        assertEquals(listOf("new-late", "new-early"), dao.getOperationsByAccount("a", 2).first().map { it.id })
    }

    @Test
    fun dailyTotalsCountMoneyInAsPositiveAndMoneyOutAsNegative() = runTest {
        dao.insertOperations(
            listOf(
                operationEntity("revenue", "a", "REVENUE", 100.0, date = "2026-10-01"),
                operationEntity("expense", "a", "EXPENSE", 30.0, date = "2026-10-01"),
                operationEntity("in", "a", "TRANSFER", 50.0, date = "2026-10-02", isIncoming = true),
                operationEntity("out", "a", "TRANSFER", 20.0, date = "2026-10-02"),
                operationEntity("other-account", "b", "REVENUE", 999.0, date = "2026-10-01"),
            )
        )

        val totals = dao.getDailyTotalsByAccount("a").first().associate { it.date to it.total }

        assertEquals(mapOf("2026-10-01" to 70.0, "2026-10-02" to 30.0), totals)
    }

    @Test
    fun operationCountByCategoryFollowsChanges() = runTest {
        dao.observeOperationCountByCategory("category_food").test {
            assertEquals(0, awaitItem())

            dao.insertOperations(
                listOf(
                    operationEntity("1", "a", "EXPENSE", 1.0, category = "category_food"),
                    operationEntity("2", "a", "EXPENSE", 1.0, category = "category_rent"),
                )
            )
            assertEquals(1, awaitItem())

            dao.deleteOperationById("1")
            assertEquals(0, awaitItem())
        }
    }

    @Test
    fun linkedOperationsAreBothSidesOfATransfer() = runTest {
        dao.insertOperations(
            listOf(
                operationEntity("out", "a", "TRANSFER", 10.0, linkedOperationId = "t", toAccountId = "b"),
                operationEntity("in", "b", "TRANSFER", 10.0, linkedOperationId = "t", toAccountId = "a", isIncoming = true),
                operationEntity("other", "a", "TRANSFER", 10.0, linkedOperationId = "t2", toAccountId = "b"),
            )
        )

        assertEquals(setOf("out", "in"), dao.getLinkedOperations("t").map { it.id }.toSet())
    }

    @Test
    fun transferCounterpartsAreTheOtherAccountsSides() = runTest {
        dao.insertOperations(
            listOf(
                // Transfer a -> b: b's side points at a
                operationEntity("a-out", "a", "TRANSFER", 10.0, linkedOperationId = "t1", toAccountId = "b"),
                operationEntity("b-in", "b", "TRANSFER", 10.0, linkedOperationId = "t1", toAccountId = "a", isIncoming = true),
                // Transfer c -> a: c's side points at a
                operationEntity("c-out", "c", "TRANSFER", 5.0, linkedOperationId = "t2", toAccountId = "a"),
                operationEntity("a-in", "a", "TRANSFER", 5.0, linkedOperationId = "t2", toAccountId = "c", isIncoming = true),
                // Not a transfer, even with to_account_id set
                operationEntity("b-expense", "b", "EXPENSE", 1.0, toAccountId = "a"),
            )
        )

        assertEquals(setOf("b-in", "c-out"), dao.getTransferCounterparts("a").map { it.id }.toSet())
    }

    @Test
    fun deleteByIdsAndByAccountRemoveOnlyThoseOperations() = runTest {
        dao.insertOperations(
            listOf(
                operationEntity("1", "a", "EXPENSE", 1.0),
                operationEntity("2", "a", "EXPENSE", 1.0),
                operationEntity("3", "b", "EXPENSE", 1.0),
                operationEntity("4", "b", "EXPENSE", 1.0),
            )
        )

        dao.deleteOperationsByIds(listOf("1", "3"))
        assertEquals(setOf("2", "4"), dao.getAllOperations().first().map { it.id }.toSet())

        dao.deleteOperationsByAccount("b")
        assertEquals(listOf("2"), dao.getAllOperations().first().map { it.id })
    }

    @Test
    fun isIncomingDefaultsToFalse() = runTest {
        dao.insertOperation(operationEntity("1", "a", "EXPENSE", 1.0))

        assertFalse(dao.getOperationById("1")!!.isIncoming)
    }
}
