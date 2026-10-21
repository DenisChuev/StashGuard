package dc.stashguard.feature.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OperationsViewModel(
    operationRepository: OperationRepository,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository,
) : ViewModel() {
    /**
     * Newest date first, and within a day the latest added first, as the list groups them by day.
     * Null until the first database read, so the empty state doesn't flash while loading.
     */
    val operations: StateFlow<List<Operation>?> = operationRepository.observeOperations()
        .map { operations ->
            operations.withoutReceivingTransferSides()
                .sortedWith(compareByDescending<Operation> { it.date }.thenByDescending { it.createdAt })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /** Category id -> category, for each row's name and icon. Operations store the category id. */
    val categories: StateFlow<Map<String, Category>> = categoryRepository.observeCategories()
        .map { categories -> categories.associateBy { it.id } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /** Account id -> name, for both sides of transfers. */
    val accountNames: StateFlow<Map<String, String>> = accountRepository.observeAccounts()
        .map { accounts -> accounts.associate { it.id to it.name } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
}

/**
 * A transfer is stored as two operations, one per account. Keeps only its sending side, so each
 * transfer is listed (and summed) once. A receiving side whose sending side is gone stays.
 */
private fun List<Operation>.withoutReceivingTransferSides(): List<Operation> {
    val sentTransferIds = filter { it.type == OperationType.TRANSFER && !it.isIncoming }
        .mapNotNull { it.linkedOperationId }
        .toSet()
    return filterNot {
        it.type == OperationType.TRANSFER && it.isIncoming && it.linkedOperationId in sentTransferIds
    }
}
