package dc.stashguard.core.domain.fake

import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [AccountRepository] that behaves like the Room one. */
class FakeAccountRepository(vararg initial: Account) : AccountRepository {
    private val accounts = MutableStateFlow(initial.toList())

    /** Every account, in insertion order. */
    val all: List<Account> get() = accounts.value

    fun balanceOf(id: String): Double = accounts.value.first { it.id == id }.balance

    override fun observeAccounts(): Flow<List<Account>> =
        accounts.map { list -> list.sortedWith(compareBy({ it.position }, { it.name.lowercase() })) }

    override fun observeAccount(id: String): Flow<Account?> =
        accounts.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getAccount(id: String): Account? = accounts.value.firstOrNull { it.id == id }

    override suspend fun getMaxPosition(): Int = accounts.value.maxOfOrNull { it.position } ?: -1

    override suspend fun addAccount(account: Account) {
        accounts.update { list -> list.filterNot { it.id == account.id } + account }
    }

    override suspend fun updateAccount(account: Account) {
        accounts.update { list -> list.map { if (it.id == account.id) account else it } }
    }

    override suspend fun adjustBalance(id: String, delta: Double) {
        accounts.update { list -> list.map { if (it.id == id) it.copy(balance = it.balance + delta) else it } }
    }

    override suspend fun reorderAccounts(orderedIds: List<String>) {
        accounts.update { list ->
            list.map { account ->
                val index = orderedIds.indexOf(account.id)
                if (index >= 0) account.copy(position = index) else account
            }
        }
    }

    override suspend fun deleteAccount(id: String) {
        accounts.update { list -> list.filterNot { it.id == id } }
    }
}
