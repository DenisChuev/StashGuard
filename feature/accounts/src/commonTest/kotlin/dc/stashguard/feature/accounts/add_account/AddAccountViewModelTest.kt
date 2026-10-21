package dc.stashguard.feature.accounts.add_account

import dc.stashguard.core.domain.usecase.AddAccountUseCase
import dc.stashguard.core.testing.FakeAccountRepository
import dc.stashguard.core.testing.MainDispatcherTest
import dc.stashguard.core.testing.account
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddAccountViewModelTest : MainDispatcherTest() {
    private val accounts = FakeAccountRepository(account("existing", position = 0))

    @Test
    fun addsTheAccountAfterTheExistingOnes() = runTest {
        val viewModel = AddAccountViewModel(AddAccountUseCase(accounts))
        var added = false

        viewModel.addAccount(account("new", balance = 250.0), onSuccess = { added = true })

        val saved = accounts.all.single { it.id == "new" }
        assertEquals(250.0, saved.balance)
        assertEquals(1, saved.position)
        assertTrue(added)
    }

    @Test
    fun repeatedTapsAddOnce() = runTest {
        val viewModel = AddAccountViewModel(AddAccountUseCase(accounts))

        viewModel.addAccount(account("new"), onSuccess = {})
        viewModel.addAccount(account("new-again"), onSuccess = {})

        assertEquals(listOf("existing", "new"), accounts.all.map { it.id })
    }
}
