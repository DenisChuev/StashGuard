package dc.stashguard.feature.accounts.accounts_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.usecase.DeleteAccountUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val logger = Logger.withTag("AccountsViewModel")

class AccountsViewModel(
    private val accountRepository: AccountRepository,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel() {
    val accounts: StateFlow<List<Account>> = accountRepository.observeAccounts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.updateAccount(account)
        }
    }

    fun reorderAccounts(orderedIds: List<String>) {
        viewModelScope.launch {
            accountRepository.reorderAccounts(orderedIds)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            deleteAccountUseCase(account.id)
        }
    }
}
