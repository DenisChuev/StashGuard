package dc.stashguard.feature.accounts.edit_account

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.usecase.DeleteAccountUseCase
import dc.stashguard.core.testing.FakeAccountRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.FakeTransactionRunner
import dc.stashguard.core.testing.MainDispatcherTest
import dc.stashguard.core.testing.account
import dc.stashguard.core.testing.keepCollecting
import dc.stashguard.core.testing.operation
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditAccountViewModelTest : MainDispatcherTest() {
    private val accounts = FakeAccountRepository(
        account("a", balance = 1234.5, position = 2).copy(name = "Wallet", isDebt = true)
    )
    private val operations = FakeOperationRepository()

    // `account` and `isLoading` only update while collected, as the screen does; saving reads `account`
    private fun TestScope.viewModel() = EditAccountViewModel(
        accountRepository = accounts,
        deleteAccountUseCase = DeleteAccountUseCase(accounts, operations, FakeTransactionRunner()),
        accountId = "a",
    ).also {
        keepCollecting(it.account)
        keepCollecting(it.isLoading)
    }

    @Test
    fun loadsTheAccountIntoTheFields() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.isLoading.value)
        assertEquals("Wallet", viewModel.editingAccountName.value)
        assertEquals("1 234.5", viewModel.editingAccountBalance.value)
    }

    @Test
    fun savesTheEditsAndKeepsTheOtherFields() = runTest {
        val viewModel = viewModel()
        var saved = false
        viewModel.updateEditingName(" Cash ")
        viewModel.updateEditingBalance("2 000.25")
        viewModel.updateEditingColor(Color(0xFF00897B))

        viewModel.saveEditedAccount(onSuccess = { saved = true })

        val account = accounts.all.single()
        assertEquals("Cash", account.name)
        assertEquals(2000.25, account.balance)
        assertEquals(Color(0xFF00897B).toArgb(), account.colorArgb)
        assertTrue(account.isDebt)
        assertEquals(2, account.position)
        assertTrue(saved)
    }

    @Test
    fun emptyBalanceSavesAsZero() = runTest {
        val viewModel = viewModel()
        viewModel.updateEditingBalance("")

        viewModel.saveEditedAccount(onSuccess = {})

        assertEquals(0.0, accounts.balanceOf("a"))
    }

    @Test
    fun invalidInputIsRejected() = runTest {
        val cases = listOf(
            Triple(" ", "100", "Account name is required."),
            Triple("Cash", "abc", "Balance must be a valid number."),
            Triple("Cash", "2000000000", "Balance must be between -1B and 1B."),
        )
        for ((name, balance, error) in cases) {
            val viewModel = viewModel()
            viewModel.updateEditingName(name)
            viewModel.updateEditingBalance(balance)

            viewModel.saveEditedAccount(onSuccess = {})

            assertEquals(error, viewModel.error.value, "name '$name', balance '$balance'")
            assertEquals(1234.5, accounts.balanceOf("a"))
        }
    }

    // Regression (4 Oct): deleting from Edit Account left the account's operations behind
    @Test
    fun deletingRemovesTheAccountAndItsOperations() = runTest {
        operations.addOperations(listOf(operation("e", "a", OperationType.EXPENSE, 10.0)))
        val viewModel = viewModel()
        var deleted = false

        viewModel.deleteAccount(onSuccess = { deleted = true })

        assertTrue(accounts.all.isEmpty())
        assertTrue(operations.all.isEmpty())
        assertTrue(deleted)
    }
}
