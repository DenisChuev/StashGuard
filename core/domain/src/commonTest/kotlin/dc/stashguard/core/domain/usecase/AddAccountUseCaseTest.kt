package dc.stashguard.core.domain.usecase

import dc.stashguard.core.testing.FakeAccountRepository
import dc.stashguard.core.testing.account
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AddAccountUseCaseTest {
    @Test
    fun firstAccountGoesToPositionZero() = runTest {
        val accounts = FakeAccountRepository()

        AddAccountUseCase(accounts)(account("a", position = 7))

        assertEquals(0, accounts.all.single().position)
    }

    @Test
    fun newAccountGoesAfterTheLastOne() = runTest {
        val accounts = FakeAccountRepository(account("a", position = 0), account("b", position = 3))

        AddAccountUseCase(accounts)(account("c"))

        assertEquals(4, accounts.all.first { it.id == "c" }.position)
    }
}
