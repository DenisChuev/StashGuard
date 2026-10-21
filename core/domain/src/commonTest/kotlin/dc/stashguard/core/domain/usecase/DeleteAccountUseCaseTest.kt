package dc.stashguard.core.domain.usecase

import dc.stashguard.core.testing.FakeAccountRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.FakeTransactionRunner
import dc.stashguard.core.testing.account
import dc.stashguard.core.testing.operation
import dc.stashguard.core.testing.transfer
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class DeleteAccountUseCaseTest {
    private val accounts = FakeAccountRepository(account("a", balance = 900.0), account("b", balance = 100.0))
    private val operations = FakeOperationRepository()
    private val deleteAccount = DeleteAccountUseCase(accounts, operations, FakeTransactionRunner())

    // Regression (4 Oct): deleting an account from some screens left its operations behind
    @Test
    fun deletesTheAccountWithItsOperations() = runTest {
        operations.addOperations(
            listOf(
                operation("e", "a", OperationType.EXPENSE, 10.0),
                operation("other", "b", OperationType.EXPENSE, 10.0),
            )
        )

        deleteAccount("a")

        assertEquals(listOf("b"), accounts.all.map { it.id })
        assertEquals(listOf("other"), operations.all.map { it.id })
    }

    @Test
    fun receivingSideInTheOtherAccountBecomesARevenue() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))

        deleteAccount("a")

        val kept = operations.all.single()
        assertEquals(receiving.id, kept.id)
        assertEquals(OperationType.REVENUE, kept.type)
        assertEquals(TRANSFER_CATEGORY_ID, kept.category)
        assertNull(kept.linkedOperationId)
        assertNull(kept.toAccountId)
        assertFalse(kept.isIncoming)
        // The other account's history and balance don't change
        assertEquals(100.0, accounts.balanceOf("b"))
    }

    @Test
    fun sendingSideInTheOtherAccountBecomesAnExpense() = runTest {
        val (sending, receiving) = transfer("b", "a", 100.0)
        operations.addOperations(listOf(sending, receiving))

        deleteAccount("a")

        val kept = operations.all.single()
        assertEquals(sending.id, kept.id)
        assertEquals(OperationType.EXPENSE, kept.type)
        assertNull(kept.linkedOperationId)
        assertEquals(100.0, accounts.balanceOf("b"))
    }
}
