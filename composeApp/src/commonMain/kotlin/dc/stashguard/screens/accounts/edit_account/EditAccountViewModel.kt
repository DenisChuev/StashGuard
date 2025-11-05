package dc.stashguard.screens.accounts.edit_account

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.data.local.AccountDao
import dc.stashguard.model.Account
import dc.stashguard.model.toAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dc.stashguard.model.toAccountEntity

class EditAccountViewModel(
    private val accountDao: AccountDao,
    private val accountId: String
) : ViewModel() {

    // --- State for loading the initial account ---
    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    // --- State for editing ---
    private val _editingAccountName = MutableStateFlow("")
    val editingAccountName: StateFlow<String> = _editingAccountName.asStateFlow()

    private val _editingAccountBalance = MutableStateFlow("")
    val editingAccountBalance: StateFlow<String> = _editingAccountBalance.asStateFlow()

    private val _editingAccountColor = MutableStateFlow(Color(0xFF2196F3))
    val editingAccountColor: StateFlow<Color> = _editingAccountColor.asStateFlow()

    private val _editingAccountIsDebt = MutableStateFlow(false)
    val editingAccountIsDebt: StateFlow<Boolean> = _editingAccountIsDebt.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAccountDetails()
    }

    fun loadAccountDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val accountEntity = accountDao.getAccountById(accountId)
                if (accountEntity != null) {
                    val account = accountEntity.toAccount()
                    _account.value = account
                    _editingAccountName.value = account.name
                    _editingAccountBalance.value = account.balance.toString()
                    _editingAccountColor.value = account.color
                    _editingAccountIsDebt.value = account.isDebt
                } else {
                    _error.value = "Account not found"
                }
            } catch (e: Exception) {
                _error.value = "Error loading account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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
    fun saveEditedAccount() {
        val name = _editingAccountName.value
        val balanceString = _editingAccountBalance.value
        val color = _editingAccountColor.value
        val isDebt = _editingAccountIsDebt.value

        // Validate inputs before saving
        if (name.isBlank() || balanceString.isBlank()) {
            _error.value = "Name and Balance are required."
            return
        }

        val balance = balanceString.toDoubleOrNull()
        if (balance == null) {
            _error.value = "Balance must be a valid number."
            return
        }

        // Create the updated Account object with the new values
        val updatedAccount = _account.value?.copy(
            name = name,
            balance = balance,
            color = color,
            isDebt = isDebt
        )

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (updatedAccount != null) {
                    // Update the database using the DAO
                    accountDao.updateAccount(updatedAccount.toAccountEntity())
                    // Optionally, update the main account state flow with the new data
                    _account.value = updatedAccount
                } else {
                    // This shouldn't happen if loadAccountDetails succeeded, but good to check
                    _error.value = "Account data not available for update."
                }
            } catch (e: Exception) {
                _error.value = "Error saving account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}