package dc.stashguard.screens.accounts.accounts_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.data.local.AccountDao
import dc.stashguard.model.Account
import dc.stashguard.model.toAccount
import dc.stashguard.model.toAccountEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val logger = Logger.withTag("AccountsViewModel")

class AccountsViewModel(private val accountDao: AccountDao) : ViewModel() {
    val accounts: StateFlow<List<Account>> = accountDao.getAllAccounts().map { entities ->
        entities.map { it.toAccount() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            accountDao.updateAccount(account.toAccountEntity())
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountDao.deleteAccount(account.toAccountEntity())
        }
    }
}