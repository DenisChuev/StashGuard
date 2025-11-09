@file:OptIn(ExperimentalTime::class)

package dc.stashguard.screens.accounts.edit_account

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.data.local.AccountDao
import dc.stashguard.model.Account
import dc.stashguard.model.toAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dc.stashguard.model.toAccountEntity
import dc.stashguard.util.toBalanceDouble
import dc.stashguard.util.toBalanceString
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlin.math.pow
import kotlin.time.ExperimentalTime

private val logger = Logger.withTag("EditAccountViewModel")

class EditAccountViewModel(
    private val accountDao: AccountDao,
    private val accountId: String
) : ViewModel() {

    // --- Reactive account stream from database ---
    private val _accountFlow = accountDao.getAccountById(accountId)
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    // Public account state
    val account: StateFlow<Account?> = _accountFlow
        .map { entity -> entity?.toAccount() }
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

    private val _editingAccountColor = MutableStateFlow(Color(0xFF2196F3))
    val editingAccountColor: StateFlow<Color> = _editingAccountColor.asStateFlow()

    private val _editingAccountIsDebt = MutableStateFlow(false)
    val editingAccountIsDebt: StateFlow<Boolean> = _editingAccountIsDebt.asStateFlow()

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

    // --- Initialize editing fields when account loads ---
    init {
        viewModelScope.launch {
            _accountFlow.collect { entity ->
                entity?.let { accountEntity ->
                    val account = accountEntity.toAccount()
                    // Initialize editing fields with current account data
                    _editingAccountName.value = account.name
                    _editingAccountBalance.value = account.balance.toBalanceString()
                    _editingAccountColor.value = account.color
                    _editingAccountIsDebt.value = account.isDebt
                }
            }
        }
    }

    // --- Editing actions ---
    fun updateEditingName(newName: String) {
        _editingAccountName.value = newName
    }

    fun updateEditingBalance(newBalance: String) {
        _editingAccountBalance.value = newBalance
    }

    fun updateEditingColor(newColor: Color) {
        _editingAccountColor.value = newColor
    }

    fun updateEditingIsDebt(isDebt: Boolean) {
        _editingAccountIsDebt.value = isDebt
    }

    // --- Function to save the edited account back to the database ---
    fun saveEditedAccount(): Boolean {
        val name = _editingAccountName.value.trim()
        val balanceString = _editingAccountBalance.value.trim()
        val color = _editingAccountColor.value
        val isDebt = _editingAccountIsDebt.value

        // Validate inputs before saving
        if (name.isBlank()) {
            _error.value = "Account name is required."
            return false
        }

        if (balanceString.isBlank()) {
            _error.value = "Balance is required."
            return false
        }

        val balance = balanceString.toBalanceDouble()
        if (balance == null) {
            _error.value = "Balance must be a valid number."
            return false
        }

        viewModelScope.launch {
            try {
                // Get current account to preserve other fields
                val currentAccount = account.value
                if (currentAccount != null) {
                    // Create the updated Account object
                    val updatedAccount = currentAccount.copy(
                        name = name,
                        balance = balance,
                        color = color,
                        isDebt = isDebt
                    )

                    // Update the database - this will automatically trigger the flow update
                    accountDao.updateAccount(updatedAccount.toAccountEntity())
                } else {
                    _error.value = "Account data not available for update."
                }
            } catch (e: Exception) {
                _error.value = "Error saving account: ${e.message}"
            }
        }

        return true
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        logger.d("Trying to delete account: $accountId")
        viewModelScope.launch {
            try {
                accountDao.deleteAccountById(accountId)
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