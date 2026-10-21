package dc.stashguard.feature.operations.edit_operation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.core.common.toBalanceString
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.categoryType
import dc.stashguard.core.domain.model.isTransfer
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.repository.OperationRepository
import dc.stashguard.core.domain.usecase.DeleteOperationUseCase
import dc.stashguard.core.domain.usecase.LinkedOperationNotFoundException
import dc.stashguard.core.domain.usecase.UpdateOperationUseCase
import dc.stashguard.feature.operations.add_operation.OperationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditOperationViewModel(
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val operationRepository: OperationRepository,
    private val updateOperationUseCase: UpdateOperationUseCase,
    private val deleteOperationUseCase: DeleteOperationUseCase,
    private val operationId: String
) : ViewModel() {

    private val _state = MutableStateFlow(OperationState())
    val state: StateFlow<OperationState> = _state.asStateFlow()

    private val originalOperation = MutableStateFlow<Operation?>(null)
    val operation: StateFlow<Operation?> = originalOperation.asStateFlow()

    // A transfer whose other side was deleted with its account can't be edited, only deleted
    private val _isReadOnly = MutableStateFlow(false)
    val isReadOnly: StateFlow<Boolean> = _isReadOnly.asStateFlow()

    val availableAccounts: Flow<List<Account>> = accountRepository.observeAccounts()

    val availableCategories: Flow<List<Category>> = combine(
        categoryRepository.observeCategories(),
        originalOperation
    ) { categories, operation ->
        if (operation == null) return@combine emptyList()
        val filterType = operation.type.categoryType
        // Transfer is set by transfers themselves, so it isn't offered for expenses or revenues
        categories.filter { category ->
            !category.isTransfer && (category.type == filterType || category.type == CategoryType.BOTH)
        }
    }

    init {
        loadOperation()
    }

    private fun loadOperation() {
        viewModelScope.launch {
            try {
                val operation = operationRepository.getOperation(operationId)
                if (operation != null) {
                    _isReadOnly.value = operation.isTransferWithoutOtherSide()
                    originalOperation.value = operation
                    _state.value = OperationState(
                        // Raw digits for the field, e.g. "200300" rather than "200300.0"
                        amount = operation.amount.toBalanceString().filterNot { it == ' ' },
                        categoryId = operation.category,
                        toAccountId = operation.toAccountId ?: "",
                        date = operation.date,
                        note = operation.note,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _state.update { it.copy(error = "Operation not found") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error loading operation: ${e.message}") }
            }
        }
    }

    fun updateAmount(amount: String) {
        _state.update { it.copy(amount = amount) }
    }

    fun updateCategory(categoryId: String) {
        _state.update { it.copy(categoryId = categoryId) }
    }

    fun updateToAccount(accountId: String) {
        _state.update { it.copy(toAccountId = accountId) }
    }

    fun updateDate(date: kotlinx.datetime.LocalDate) {
        _state.update { it.copy(date = date) }
    }

    fun updateNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun saveOperation(onSuccess: () -> Unit) {
        val currentState = _state.value
        val originalOp = originalOperation.value
        val amount = currentState.amount.toDoubleOrNull()

        if (originalOp == null) {
            _state.update { it.copy(error = "Original operation data not loaded") }
            return
        }

        when {
            currentState.amount.isBlank() -> {
                _state.update { it.copy(error = "Amount is required") }
                return
            }

            amount == null -> {
                _state.update { it.copy(error = "Amount must be a valid number") }
                return
            }

            amount <= 0 -> {
                _state.update { it.copy(error = "Amount must be positive") }
                return
            }

            originalOp.type == OperationType.TRANSFER && currentState.toAccountId.isBlank() -> {
                _state.update { it.copy(error = "Please select destination account") }
                return
            }

            originalOp.type == OperationType.TRANSFER && currentState.toAccountId == originalOp.accountId -> {
                _state.update { it.copy(error = "Please select a different account") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                updateOperationUseCase(
                    original = originalOp,
                    amount = amount,
                    categoryId = currentState.categoryId,
                    date = currentState.date,
                    note = currentState.note,
                    toAccountId = currentState.toAccountId,
                )
                onSuccess() // Notify the UI to navigate back
            } catch (e: LinkedOperationNotFoundException) {
                _state.update { it.copy(error = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error saving operation: ${e.message}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteOperation(onSuccess: () -> Unit) {
        val operation = originalOperation.value ?: return
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                deleteOperationUseCase(operation)
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error deleting operation: ${e.message}") }
            }
        }
    }

    private suspend fun Operation.isTransferWithoutOtherSide(): Boolean {
        if (type != OperationType.TRANSFER) return false
        val linkedId = linkedOperationId ?: return true
        return operationRepository.getLinkedOperations(linkedId).none { it.id != id }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
