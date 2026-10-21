package dc.stashguard.core.domain.usecase

import dc.stashguard.core.testing.FakeAccountRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.FakeTransactionRunner
import dc.stashguard.core.testing.account
import dc.stashguard.core.testing.operation
import dc.stashguard.core.testing.transfer
import dc.stashguard.core.domain.model.OperationType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteOperationUseCaseTest {
    private val accounts = FakeAccountRepository(account("a", balance = 900.0), account("b", balance = 100.0))
    private val operations = FakeOperationRepository()
    private val deleteOperation = DeleteOperationUseCase(accounts, operations, FakeTransactionRunner())

    @Test
    fun deletingAnExpenseGivesTheMoneyBack() = runTest {
        val expense = operation("e", "a", OperationType.EXPENSE, 100.0)
        operations.addOperations(listOf(expense))

        deleteOperation(expense)

        assertEquals(1000.0, accounts.balanceOf("a"))
        assertTrue(operations.all.isEmpty())
    }

    @Test
    fun deletingARevenueTakesTheMoneyBack() = runTest {
        val revenue = operation("r", "a", OperationType.REVENUE, 100.0)
        operations.addOperations(listOf(revenue))

        deleteOperation(revenue)

        assertEquals(800.0, accounts.balanceOf("a"))
    }

    @Test
    fun deletingEitherTransferSideDeletesBothAndRestoresBothBalances() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))

        deleteOperation(receiving)

        assertEquals(1000.0, accounts.balanceOf("a"))
        assertEquals(0.0, accounts.balanceOf("b"))
        assertTrue(operations.all.isEmpty())
    }

    @Test
    fun deletingATransferWithoutItsOtherSideRestoresOnlyItsAccount() = runTest {
        val (sending, _) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending))

        deleteOperation(sending)

        assertEquals(1000.0, accounts.balanceOf("a"))
        assertEquals(100.0, accounts.balanceOf("b"))
        assertTrue(operations.all.isEmpty())
    }
}
