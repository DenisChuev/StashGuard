package dc.stashguard.core.domain.usecase

import dc.stashguard.core.domain.fake.FakeAccountRepository
import dc.stashguard.core.domain.fake.FakeOperationRepository
import dc.stashguard.core.domain.fake.FakeTransactionRunner
import dc.stashguard.core.domain.fake.TEST_DATE
import dc.stashguard.core.domain.fake.account
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AddOperationUseCaseTest {
    private val accounts = FakeAccountRepository(account("a", balance = 1000.0), account("b", balance = 50.0))
    private val operations = FakeOperationRepository()
    private val transaction = FakeTransactionRunner()
    private val addOperation = AddOperationUseCase(accounts, operations, transaction)

    private suspend fun add(type: OperationType, amount: Double, accountId: String = "a", toAccountId: String = "") =
        addOperation(
            accountId = accountId,
            type = type,
            amount = amount,
            categoryId = "category_food",
            date = TEST_DATE,
            note = "note",
            toAccountId = toAccountId,
        )

    @Test
    fun revenueIsStoredAndAddedToTheBalance() = runTest {
        add(OperationType.REVENUE, 200.0)

        assertEquals(1200.0, accounts.balanceOf("a"))
        val operation = operations.all.single()
        assertEquals("a", operation.accountId)
        assertEquals(OperationType.REVENUE, operation.type)
        assertEquals(200.0, operation.amount)
        assertEquals("category_food", operation.category)
        assertEquals(1, transaction.transactionCount)
    }

    @Test
    fun expenseIsSubtractedFromTheBalance() = runTest {
        add(OperationType.EXPENSE, 300.0)

        assertEquals(700.0, accounts.balanceOf("a"))
        assertEquals(OperationType.EXPENSE, operations.all.single().type)
    }

    @Test
    fun transferMovesMoneyAndStoresBothSides() = runTest {
        add(OperationType.TRANSFER, 400.0, accountId = "a", toAccountId = "b")

        assertEquals(600.0, accounts.balanceOf("a"))
        assertEquals(450.0, accounts.balanceOf("b"))

        val sending = operations.all.single { it.accountId == "a" }
        val receiving = operations.all.single { it.accountId == "b" }
        assertFalse(sending.isIncoming)
        assertTrue(receiving.isIncoming)
        assertEquals("b", sending.toAccountId)
        assertEquals("a", receiving.toAccountId)
        assertNotNull(sending.linkedOperationId)
        assertEquals(sending.linkedOperationId, receiving.linkedOperationId)
        // Transfers always use the built-in category, whatever the caller passed
        assertEquals(TRANSFER_CATEGORY_ID, sending.category)
        assertEquals(TRANSFER_CATEGORY_ID, receiving.category)
        assertEquals(1, transaction.transactionCount)
    }

    @Test
    fun transferToTheSameAccountIsRejectedWithoutWriting() = runTest {
        assertFailsWith<IllegalArgumentException> {
            add(OperationType.TRANSFER, 400.0, accountId = "a", toAccountId = "a")
        }

        assertEquals(1000.0, accounts.balanceOf("a"))
        assertTrue(operations.all.isEmpty())
    }
}
