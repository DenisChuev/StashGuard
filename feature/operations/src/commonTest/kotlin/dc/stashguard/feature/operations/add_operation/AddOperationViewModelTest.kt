package dc.stashguard.feature.operations.add_operation

import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import dc.stashguard.core.domain.usecase.AddOperationUseCase
import dc.stashguard.core.testing.FakeAccountRepository
import dc.stashguard.core.testing.FakeCategoryRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.FakeTransactionRunner
import dc.stashguard.core.testing.MainDispatcherTest
import dc.stashguard.core.testing.account
import dc.stashguard.core.testing.category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddOperationViewModelTest : MainDispatcherTest() {
    private val accounts = FakeAccountRepository(account("a", balance = 1000.0), account("b", balance = 0.0))
    private val categories = FakeCategoryRepository(
        category("food", CategoryType.EXPENSE),
        category("salary", CategoryType.REVENUE),
        category(TRANSFER_CATEGORY_ID, CategoryType.BOTH),
    )
    private val operations = FakeOperationRepository()

    private fun viewModel(type: OperationType) = AddOperationViewModel(
        accountRepository = accounts,
        categoryRepository = categories,
        addOperationUseCase = AddOperationUseCase(accounts, operations, FakeTransactionRunner()),
        accountId = "a",
        operationType = type,
    )

    @Test
    fun invalidInputIsRejectedWithoutSaving() = runTest {
        val cases = listOf(
            Triple(OperationType.EXPENSE, "", "") to "Amount is required",
            Triple(OperationType.EXPENSE, "abc", "") to "Amount must be a valid number",
            Triple(OperationType.EXPENSE, "0", "") to "Amount must be positive",
            Triple(OperationType.TRANSFER, "100", "") to "Please select destination account",
            Triple(OperationType.TRANSFER, "100", "a") to "Please select a different account",
        )
        for ((input, error) in cases) {
            val (type, amount, toAccountId) = input
            val viewModel = viewModel(type)
            var saved = false
            viewModel.updateAmount(amount)
            viewModel.updateToAccount(toAccountId)

            viewModel.saveOperation(onSuccess = { saved = true })

            assertEquals(error, viewModel.state.value.error, "$type, amount '$amount', to '$toAccountId'")
            assertEquals(false, saved)
        }
        assertTrue(operations.all.isEmpty())
        assertEquals(1000.0, accounts.balanceOf("a"))
    }

    @Test
    fun savesAnExpenseWithItsCategory() = runTest {
        val viewModel = viewModel(OperationType.EXPENSE)
        var saved = false
        viewModel.updateAmount("250.5")
        viewModel.updateCategory("food")
        viewModel.updateNote("Lunch")

        viewModel.saveOperation(onSuccess = { saved = true })

        val operation = operations.all.single()
        assertEquals(250.5, operation.amount)
        assertEquals("food", operation.category)
        assertEquals("Lunch", operation.note)
        assertEquals(749.5, accounts.balanceOf("a"))
        assertTrue(saved)
    }

    @Test
    fun transferUsesTheTransferCategoryAndMovesMoney() = runTest {
        val viewModel = viewModel(OperationType.TRANSFER)
        assertEquals(TRANSFER_CATEGORY_ID, viewModel.state.value.categoryId)
        viewModel.updateAmount("300")
        viewModel.updateToAccount("b")

        viewModel.saveOperation(onSuccess = {})

        assertEquals(700.0, accounts.balanceOf("a"))
        assertEquals(300.0, accounts.balanceOf("b"))
        assertEquals(2, operations.all.size)
    }

    @Test
    fun expenseCategoriesIncludeBothTypeCategories() = runTest {
        val viewModel = viewModel(OperationType.EXPENSE)

        assertEquals(setOf("food", TRANSFER_CATEGORY_ID), viewModel.availableCategories.first().map { it.id }.toSet())
    }
}
