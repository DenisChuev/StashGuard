package dc.stashguard.feature.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OperationsViewModel(
    operationRepository: OperationRepository
) : ViewModel() {
    val operations: StateFlow<List<Operation>> = operationRepository.observeOperations()
        .map { operations -> operations.sortedByDescending { it.createdAt } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
