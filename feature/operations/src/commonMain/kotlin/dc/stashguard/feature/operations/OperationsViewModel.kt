package dc.stashguard.feature.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OperationsViewModel(
    operationRepository: OperationRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    val operations: StateFlow<List<Operation>> = operationRepository.observeOperations()
        .map { operations -> operations.withoutReceivingTransferSides().sortedByDescending { it.createdAt } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Category id -> name. Operations store the category id. */
    val categoryNames: StateFlow<Map<String, String>> = categoryRepository.observeCategories()
        .map { categories -> categories.associate { it.id to it.name } }
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
