package dc.stashguard.feature.accounts.add_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.usecase.AddAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val logger = Logger.withTag("AddAccountViewModel")

class AddAccountViewModel(private val addAccountUseCase: AddAccountUseCase) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        logger.d { "AddAccountViewModel created" }
    }

    fun addAccount(account: Account, onSuccess: () -> Unit) {
        // Ignore repeated taps while the first insert is running
        if (_isSaving.value) return
        _isSaving.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                logger.d { "Inserting account: $account" }
                addAccountUseCase(account)
                logger.d { "Account inserted successfully" }
                // isSaving stays true so the button is not tappable while the screen closes
                onSuccess()
            } catch (e: Exception) {
                logger.e(e) { "Error inserting account" }
                _error.value = "Error saving account: ${e.message}"
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
