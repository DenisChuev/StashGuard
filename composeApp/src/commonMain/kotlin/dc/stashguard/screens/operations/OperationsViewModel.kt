package dc.stashguard.screens.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.data.local.AccountDao
import dc.stashguard.data.local.OperationDao
import dc.stashguard.model.Operation
import dc.stashguard.model.toOperation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OperationsViewModel(
    private val accountDao: AccountDao,
    private val operationDao: OperationDao
) : ViewModel() {
    val operations: StateFlow<List<Operation>> = operationDao.getAllOperations()
        .map { entities ->
            entities.sortedByDescending { it.createdAt }
                .map { it.toOperation() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}