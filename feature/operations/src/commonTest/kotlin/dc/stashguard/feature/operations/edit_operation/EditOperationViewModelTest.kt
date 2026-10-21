package dc.stashguard.feature.operations.edit_operation

import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import dc.stashguard.core.domain.usecase.DeleteOperationUseCase
import dc.stashguard.core.domain.usecase.UpdateOperationUseCase
import dc.stashguard.core.testing.FakeAccountRepository
import dc.stashguard.core.testing.FakeCategoryRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.FakeTransactionRunner
import dc.stashguard.core.testing.MainDispatcherTest
import dc.stashguard.core.testing.account
import dc.stashguard.core.testing.category
import dc.stashguard.core.testing.operation
import dc.stashguard.core.testing.transfer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditOperationViewModelTest : MainDispatcherTest() {
    // Balances already include the operations under test
    private val accounts = FakeAccountRepository(account("a", balance = 900.0), account("b", balance = 100.0))
    private val categories = FakeCategoryRepository(
        category("food", CategoryType.EXPENSE),
        category("salary", CategoryType.REVENUE),
    )
    private val operations = FakeOperationRepository()

    private fun viewModel(operationId: String): EditOperationViewModel {
        val transaction = FakeTransactionRunner()
        return EditOperationViewModel(
            accountRepository = accounts,
            categoryRepository = categories,
            operationRepository = operations,
            updateOperationUseCase = UpdateOperationUseCase(accounts, operations, transaction),
            deleteOperationUseCase = DeleteOperationUseCase(accounts, operations, transaction),
            operationId = operationId,
        )
    }

    @Test
    fun loadsTheOperationIntoTheForm() = runTest {
        operations.addOperations(listOf(operation("e", "a", OperationType.EXPENSE, 1234.5, category = "food")))

        val state = viewModel("e").state.value

        // Raw digits for the field, without the thousands separator
        assertEquals("1234.5", state.amount)
        assertEquals("food", state.categoryId)
        assertNull(state.error)
    }

    @Test
    fun missingOperationShowsAnError() = runTest {
        assertEquals("Operation not found", viewModel("missing").state.value.error)
    }

    @Test
    fun onlyCategoriesOfTheOperationTypeAreOffered() = runTest {
        operations.addOperations(listOf(operation("e", "a", OperationType.EXPENSE, 100.0)))

        assertEquals(listOf("food"), viewModel("e").availableCategories.first().map { it.id })
    }

    @Test
    fun savingMovesTheBalanceByTheDifference() = runTest {
        operations.addOperations(listOf(operation("e", "a", OperationType.EXPENSE, 100.0)))
        val viewModel = viewModel("e")
        var saved = false
        viewModel.updateAmount("40")

        viewModel.saveOperation(onSuccess = { saved = true })

        assertEquals(960.0, accounts.balanceOf("a"))
        assertEquals(40.0, operations.get("e")?.amount)
        assertTrue(saved)
    }

    @Test
    fun transferToItsOwnAccountIsRejected() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))
        val viewModel = viewModel(sending.id)
        viewModel.updateToAccount("a")

        viewModel.saveOperation(onSuccess = {})

        assertEquals("Please select a different account", viewModel.state.value.error)
        assertEquals(100.0, accounts.balanceOf("b"))
    }

    @Test
    fun transferWithoutItsOtherSideIsReadOnly() = runTest {
        val (sending, _) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending))

        assertTrue(viewModel(sending.id).isReadOnly.value)
    }

    @Test
    fun completeTransferIsEditable() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))

        val viewModel = viewModel(receiving.id)

        assertFalse(viewModel.isReadOnly.value)
        assertEquals("a", viewModel.state.value.toAccountId)
        assertEquals(TRANSFER_CATEGORY_ID, viewModel.state.value.categoryId)
    }

    @Test
    fun deletingATransferRemovesBothSidesAndRestoresBalances() = runTest {
        val (sending, receiving) = transfer("a", "b", 100.0)
        operations.addOperations(listOf(sending, receiving))
        val viewModel = viewModel(sending.id)
        var deleted = false

        viewModel.deleteOperation(onSuccess = { deleted = true })

        assertTrue(operations.all.isEmpty())
        assertEquals(1000.0, accounts.balanceOf("a"))
        assertEquals(0.0, accounts.balanceOf("b"))
        assertTrue(deleted)
    }
}
