package dc.stashguard.screens.accounts.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.data.local.AccountDao
import dc.stashguard.data.local.OperationDao
import dc.stashguard.model.Account
import dc.stashguard.model.Operation
import dc.stashguard.model.OperationType
import dc.stashguard.model.toAccount
import dc.stashguard.model.toOperation
import dc.stashguard.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.minus

private val logger = Logger.withTag("DetailsAccountViewModel")

class DetailsAccountViewModel(
    private val accountDao: AccountDao,
    private val operationDao: OperationDao,
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

    // --- Recent operations for this account ---
    val recentOperations: StateFlow<List<Operation>> = operationDao.getOperationsByAccount(accountId)
        .map { entities ->
            entities.take(5).map { it.toOperation() } // Last 5 operations
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Statistics for this account ---
    val accountStatistics: StateFlow<AccountStatistics> = combine(
        _accountFlow,
        operationDao.getOperationsByAccount(accountId)
    ) { accountEntity, operations ->
        val account = accountEntity?.toAccount()
        if (account != null) {
            calculateStatistics(account, operations.map { it.toOperation() })
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
                // Delete all operations for this account first
                operationDao.deleteOperationsByAccount(accountId)
                // Then delete the account
                accountDao.deleteAccountById(accountId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Error deleting account: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun calculateStatistics(account: Account, operations: List<Operation>): AccountStatistics {
        val today = DateUtils.currentDate()

        val last30DaysOperations = operations.filter { operation ->
            val operationDate = operation.date
            val daysDifference = (today - operationDate).days
            daysDifference <= 30
        }

        val totalRevenue = last30DaysOperations
            .filter { it.type == OperationType.REVENUE }
            .sumOf { it.amount }

        val totalExpenses = last30DaysOperations
            .filter { it.type == OperationType.EXPENSE}
            .sumOf { it.amount }

        val netChange = totalRevenue - totalExpenses

        return AccountStatistics(
            totalRevenue = totalRevenue,
            totalExpenses = totalExpenses,
            netChange = netChange,
            transactionCount = last30DaysOperations.size,
            averageTransaction = if (last30DaysOperations.isNotEmpty()) {
                (totalRevenue + totalExpenses) / last30DaysOperations.size
            } else 0.0
        )
    }
}

data class AccountStatistics(
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netChange: Double = 0.0,
    val transactionCount: Int = 0,
    val averageTransaction: Double = 0.0
)