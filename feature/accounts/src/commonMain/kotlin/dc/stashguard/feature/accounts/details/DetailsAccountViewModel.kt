package dc.stashguard.feature.accounts.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.AccountStatistics
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.OperationRepository
import dc.stashguard.core.domain.usecase.CalculateAccountStatisticsUseCase
import dc.stashguard.core.domain.usecase.DeleteAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val logger = Logger.withTag("DetailsAccountViewModel")

class DetailsAccountViewModel(
    accountRepository: AccountRepository,
    operationRepository: OperationRepository,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val calculateAccountStatistics: CalculateAccountStatisticsUseCase,
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

    // --- Recent operations for this account ---
    val recentOperations: StateFlow<List<Operation>> = operationRepository.observeOperationsByAccount(accountId)
        .map { operations ->
            operations.take(5) // Last 5 operations
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Statistics for this account ---
    val accountStatistics: StateFlow<AccountStatistics> = combine(
        _accountFlow,
        operationRepository.observeOperationsByAccount(accountId)
    ) { account, operations ->
        if (account != null) {
            calculateAccountStatistics(operations)
        } else {
            AccountStatistics() // Default empty stats
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountStatistics()
    )

    // --- Loading and error states ---
    val isLoading: StateFlow<Boolean> = _accountFlow
        .map { it == null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // --- Actions ---
    fun refresh() {
        viewModelScope.launch {
            _error.value = null
            // The flows will automatically update when data changes
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _error.value = null
            try {
                deleteAccountUseCase(accountId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Error deleting account: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
