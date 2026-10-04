package dc.stashguard.core.domain.repository

import dc.stashguard.core.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    /** All accounts in the user-defined order. */
    fun observeAccounts(): Flow<List<Account>>

    fun observeAccount(id: String): Flow<Account?>

    suspend fun getAccount(id: String): Account?

    /** Highest `position` in use, or -1 when there are no accounts. */
    suspend fun getMaxPosition(): Int

    suspend fun addAccount(account: Account)

    suspend fun updateAccount(account: Account)

    /** Adds [delta] to the account's balance. Does nothing if the account does not exist. */
    suspend fun adjustBalance(id: String, delta: Double)

    /** Each account's position becomes its index in [orderedIds]. */
    suspend fun reorderAccounts(orderedIds: List<String>)

    suspend fun deleteAccount(id: String)
}
