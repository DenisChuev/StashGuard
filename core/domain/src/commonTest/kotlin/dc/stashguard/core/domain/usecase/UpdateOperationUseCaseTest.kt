package dc.stashguard.core.domain.usecase

import dc.stashguard.core.testing.FakeAccountRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.FakeTransactionRunner
import dc.stashguard.core.testing.TEST_DATE
import dc.stashguard.core.testing.account
import dc.stashguard.core.testing.operation
import dc.stashguard.core.testing.transfer
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateOperationUseCaseTest {
    // Balances already include the operations under test
    private val accounts = FakeAccountRepository(
        account("a", balance = 900.0),
        account("b", balance = 100.0),
        account("c", balance = 0.0),
    )
    private val operations = FakeOperationRepository()
    private val updateOperation = UpdateOperationUseCase(accounts, operations, FakeTransactionRunner())

    private suspend fun update(
        original: Operation,
        amount: Double = original.amount,
        toAccountId: String = original.toAccountId.orEmpty(),
    ) = updateOperation(
        original = original,
        amount = amount,
        categoryId = original.category,
        date = TEST_DATE,
        note = "edited",
        toAccountId = toAccountId,
    )

    @Test
    fun revenueAmountChangeMovesTheBalanceByTheDifference() = runTest {
        val revenue = operation("r", "a", OperationType.REVENUE, 100.0)
        operations.addOperations(listOf(revenue))

        update(revenue, amount = 150.0)

        assertEquals(950.0, accounts.balanceOf("a"))
        assertEquals(150.0, operations.get("r")?.amount)
        assertEquals("edited", operations.get("r")?.note)
    }

    @Test
    fun expenseAmountChangeMovesTheBalanceByTheDifference() = runTest {
        val expense = operation("e", "a", OperationType.EXPENSE, 100.0)
        operations.addOperations(listOf(expense))

        update(expense, amount = 40.0)

        assertEquals(960.0, accounts.balanceOf("a"))
    }

    @Test
    fun editingTheSendingSideUpdatesBothSidesAndBalances() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))

        update(sending, amount = 150.0)

        assertEquals(850.0, accounts.balanceOf("a"))
        assertEquals(150.0, accounts.balanceOf("b"))
        assertEquals(150.0, operations.get(sending.id)?.amount)
        assertEquals(150.0, operations.get(receiving.id)?.amount)
    }

    // Regression (4 Oct): editing the receiving side moved balances the wrong way
    @Test
    fun editingTheReceivingSideMovesBalancesInTheTransferDirection() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))

        update(receiving, amount = 150.0)

        assertEquals(850.0, accounts.balanceOf("a"))
        assertEquals(150.0, accounts.balanceOf("b"))
        assertEquals(150.0, operations.get(sending.id)?.amount)
    }

    // Regression (4 Oct): changing the transfer account didn't move the other side
    @Test
    fun changingTheDestinationMovesTheOtherSideToTheNewAccount() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))

        update(sending, toAccountId = "c")

        assertEquals(900.0, accounts.balanceOf("a"))
        assertEquals(0.0, accounts.balanceOf("b"))
        assertEquals(100.0, accounts.balanceOf("c"))
        assertEquals("c", operations.get(sending.id)?.toAccountId)
        assertEquals("c", operations.get(receiving.id)?.accountId)
        assertEquals("a", operations.get(receiving.id)?.toAccountId)
    }

    @Test
    fun changingTheSourceFromTheReceivingSideMovesTheSendingSide() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))

        update(receiving, toAccountId = "c")

        assertEquals(1000.0, accounts.balanceOf("a"))
        assertEquals(100.0, accounts.balanceOf("b"))
        assertEquals(-100.0, accounts.balanceOf("c"))
        assertEquals("c", operations.get(sending.id)?.accountId)
        assertEquals("c", operations.get(receiving.id)?.toAccountId)
    }

    @Test
    fun changingATransferToTheSameAccountIsRejectedWithoutWriting() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))

        assertFailsWith<IllegalArgumentException> { update(sending, amount = 150.0, toAccountId = "a") }

        assertEquals(900.0, accounts.balanceOf("a"))
        assertEquals(100.0, accounts.balanceOf("b"))
        assertEquals(100.0, operations.get(sending.id)?.amount)
    }

    @Test
    fun transferWithoutItsOtherSideCantBeEdited() = runTest {
        val (sending, _) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending))

        assertFailsWith<LinkedOperationNotFoundException> { update(sending, amount = 150.0) }

        assertEquals(900.0, accounts.balanceOf("a"))
    }
}
