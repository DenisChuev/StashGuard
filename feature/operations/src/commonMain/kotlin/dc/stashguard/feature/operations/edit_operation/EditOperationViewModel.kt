package dc.stashguard.feature.operations.edit_operation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.categoryType
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.repository.OperationRepository
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
    private val operationId: String
) : ViewModel() {

    private val _state = MutableStateFlow(OperationState())
    val state: StateFlow<OperationState> = _state.asStateFlow()

    private val originalOperation = MutableStateFlow<Operation?>(null)

    val availableAccounts: Flow<List<Account>> = accountRepository.observeAccounts()

    val availableCategories: Flow<List<Category>> = combine(
        categoryRepository.observeCategories(),
        originalOperation
    ) { categories, operation ->
        if (operation == null) return@combine emptyList()
        val filterType = operation.type.categoryType
        categories.filter { category ->
            category.type == filterType || category.type == CategoryType.BOTH
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
                    originalOperation.value = operation
                    _state.value = OperationState(
                        amount = operation.amount.toString(),
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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun getOperationType(): OperationType? {
        return originalOperation.value?.type
    }

    fun isIncomingTransfer(): Boolean {
        return originalOperation.value?.isIncoming == true
    }
}