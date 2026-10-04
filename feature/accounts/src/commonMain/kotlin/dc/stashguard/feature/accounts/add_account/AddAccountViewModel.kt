package dc.stashguard.feature.accounts.add_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.usecase.AddAccountUseCase
import kotlinx.coroutines.launch

private val logger = Logger.withTag("AddAccountViewModel")

class AddAccountViewModel(private val addAccountUseCase: AddAccountUseCase) : ViewModel() {

    init {
        logger.d { "AddAccountViewModel created" }
    }

    fun addAccount(account: Account, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                logger.d { "Inserting account: $account" }
                addAccountUseCase(account)
                logger.d { "Account inserted successfully" }
                onSuccess()
            } catch (e: Exception) {
                logger.e(e) { "Error inserting account" }
            }
        }
    }
}
