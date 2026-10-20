@file:OptIn(ExperimentalCoroutinesApi::class)

package dc.stashguard.feature.accounts.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.repository.OperationRepository
import dc.stashguard.core.domain.usecase.DeleteAccountUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

private val logger = Logger.withTag("DetailsAccountViewModel")

/** Operations loaded per batch as the recent activity list scrolls. */
private const val OPERATIONS_BATCH_SIZE = 25

class DetailsAccountViewModel(
    accountRepository: AccountRepository,
    operationRepository: OperationRepository,
    categoryRepository: CategoryRepository,
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

    // --- Recent operations for this account, loaded in batches ---
    // Growing the limit re-queries a longer list, so the loaded operations stay live
    private val operationsLimit = MutableStateFlow(OPERATIONS_BATCH_SIZE)

    val recentOperations: StateFlow<List<Operation>> = operationsLimit
        .flatMapLatest { limit -> operationRepository.observeOperationsByAccount(accountId, limit) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** A full batch came back, so there may be older operations to load. */
    val canLoadMoreOperations: StateFlow<Boolean> = combine(recentOperations, operationsLimit) { operations, limit ->
        operations.size >= limit
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /** Date -> net change of the account that day, over all its operations, not just the loaded ones. */
    val dailyTotals: StateFlow<Map<LocalDate, Double>> = operationRepository.observeDailyTotalsByAccount(accountId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /** Category id -> name. Operations store the category id. */
    val categoryNames: StateFlow<Map<String, String>> = categoryRepository.observeCategories()
        .map { categories -> categories.associate { it.id to it.name } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // --- Loading and error states ---
    // Loading ends with the first emission; a null account then means "not found", not "loading"
    val isLoading: StateFlow<Boolean> = _accountFlow
        .map { false }
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

    fun loadMoreOperations() {
        // Until the current batch arrives the list is shorter than the limit, so repeated calls are ignored
        if (recentOperations.value.size >= operationsLimit.value) {
            operationsLimit.value += OPERATIONS_BATCH_SIZE
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
