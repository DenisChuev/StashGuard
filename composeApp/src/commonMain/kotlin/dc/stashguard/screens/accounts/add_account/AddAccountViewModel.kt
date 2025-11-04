package dc.stashguard.screens.accounts.add_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.data.local.AccountDao
import dc.stashguard.model.Account
import dc.stashguard.model.toAccountEntity
import kotlinx.coroutines.launch

private val logger = Logger.withTag("AddAccountViewModel")

class AddAccountViewModel(private val accountDao: AccountDao) : ViewModel() {

    init {
        logger.d { "AddAccountViewModel created" }
    }

    fun addAccount(account: Account, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                logger.d { "Inserting account: $account" }
                accountDao.insertAccount(account.toAccountEntity())
                logger.d { "Account inserted successfully" }
                onSuccess()
            } catch (e: Exception) {
                logger.e(e) { "Error inserting account" }
            }
        }
    }
}