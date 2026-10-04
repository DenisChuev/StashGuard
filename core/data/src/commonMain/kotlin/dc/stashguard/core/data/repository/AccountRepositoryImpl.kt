package dc.stashguard.core.data.repository

import dc.stashguard.core.data.mapper.toDomain
import dc.stashguard.core.data.mapper.toEntity
import dc.stashguard.core.database.dao.AccountDao
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class AccountRepositoryImpl(
    private val accountDao: AccountDao,
) : AccountRepository {
    override fun observeAccounts(): Flow<List<Account>> =
        accountDao.getAllAccounts().map { entities -> entities.map { it.toDomain() } }

    override fun observeAccount(id: String): Flow<Account?> =
        accountDao.getAccountById(id).map { it?.toDomain() }

    override suspend fun getAccount(id: String): Account? =
        accountDao.getAccountByIdOnce(id)?.toDomain()

    override suspend fun getMaxPosition(): Int = accountDao.getMaxPosition()

    override suspend fun addAccount(account: Account) {
        accountDao.insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    override suspend fun adjustBalance(id: String, delta: Double) {
        val account = accountDao.getAccountByIdOnce(id) ?: return
        accountDao.updateAccount(account.copy(balance = account.balance + delta))
    }

    override suspend fun reorderAccounts(orderedIds: List<String>) {
        accountDao.updatePositions(orderedIds)
    }

    override suspend fun deleteAccount(id: String) {
        accountDao.deleteAccountById(id)
    }
}
