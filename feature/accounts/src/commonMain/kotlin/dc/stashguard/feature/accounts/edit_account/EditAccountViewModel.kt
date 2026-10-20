@file:OptIn(ExperimentalTime::class)

package dc.stashguard.feature.accounts.edit_account

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.core.common.toBalanceDouble
import dc.stashguard.core.common.toBalanceString
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.usecase.DeleteAccountUseCase
import dc.stashguard.core.ui.color
import dc.stashguard.feature.accounts.add_account.DefaultAccountColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

private val logger = Logger.withTag("EditAccountViewModel")

class EditAccountViewModel(
    private val accountRepository: AccountRepository,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val accountId: String
) : ViewModel() {

    // --- Reactive account stream from database ---
    private val _accountFlow = accountRepository.observeAccount(accountId)
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    // Public account state
    val account: StateFlow<Account?> = _accountFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // --- State for editing ---
    private val _editingAccountName = MutableStateFlow("")
    val editingAccountName: StateFlow<String> = _editingAccountName.asStateFlow()

    private val _editingAccountBalance = MutableStateFlow("")
    val editingAccountBalance: StateFlow<String> = _editingAccountBalance.asStateFlow()

    private val _editingAccountColor = MutableStateFlow(DefaultAccountColor)
    val editingAccountColor: StateFlow<Color> = _editingAccountColor.asStateFlow()

    // --- Derived states ---
    val isLoading: StateFlow<Boolean> = _accountFlow
        .map { it == null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // --- Initialize editing fields when account loads ---
    init {
        viewModelScope.launch {
            _accountFlow.collect { loadedAccount ->
                loadedAccount?.let { account ->
                    // Initialize editing fields with current account data
                    _editingAccountName.value = account.name
                    _editingAccountBalance.value = account.balance.toBalanceString()
                    _editingAccountColor.value = account.color
                }
            }
        }
    }

    // --- Editing actions ---
    // Editing a field clears a validation error about it
    fun updateEditingName(newName: String) {
        _editingAccountName.value = newName
        _error.value = null
    }

    fun updateEditingBalance(newBalance: String) {
        _editingAccountBalance.value = newBalance
        _error.value = null
    }

    fun updateEditingColor(newColor: Color) {
        _editingAccountColor.value = newColor
    }

    // --- Function to save the edited account back to the database ---
    /** Validates and saves the edits, then calls [onSuccess] once they are in the database. */
    fun saveEditedAccount(onSuccess: () -> Unit) {
        // Ignore repeated taps while the first save is running
        if (_isSaving.value) return

        val name = _editingAccountName.value.trim()
        val balanceString = _editingAccountBalance.value.trim()
        val color = _editingAccountColor.value

        // Validate inputs before saving
        if (name.isBlank()) {
            _error.value = "Account name is required."
            return
        }

        // An empty balance means 0, as on the Add Account screen
        val balance = balanceString.ifBlank { "0" }.toBalanceDouble()
        if (balance == null) {
            _error.value = "Balance must be a valid number."
            return
        }

        // Same limit as the balance field's error state
        if (balance !in -1_000_000_000.0..1_000_000_000.0) {
            _error.value = "Balance must be between -1B and 1B."
            return
        }

        _isSaving.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                // Get current account to preserve other fields
                val currentAccount = account.value
                if (currentAccount != null) {
                    // Create the updated Account object
                    val updatedAccount = currentAccount.copy(
                        name = name,
                        balance = balance,
                        colorArgb = color.toArgb()
                    )

                    // Update the database - this will automatically trigger the flow update
                    accountRepository.updateAccount(updatedAccount)
                    // isSaving stays true so the button is not tappable while the screen closes
                    onSuccess()
                } else {
                    _error.value = "Account data not available for update."
                    _isSaving.value = false
                }
            } catch (e: Exception) {
                logger.e(e) { "Error saving account" }
                _error.value = "Error saving account: ${e.message}"
                _isSaving.value = false
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        logger.d("Trying to delete account: $accountId")
        viewModelScope.launch {
            try {
                deleteAccountUseCase(accountId)
                onSuccess()
            } catch (e: Exception) {
                logger.e("Error deleting account", e)
                _error.value = "Error deleting account: ${e.message}"
            }
        }
    }

    // Clear error when user dismisses it
    fun clearError() {
        _error.value = null
    }
}