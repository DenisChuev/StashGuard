package dc.stashguard.screens.operations.edit_operation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.data.local.AccountDao
import dc.stashguard.data.local.CategoryDao
import dc.stashguard.data.local.CategoryType
import dc.stashguard.data.local.OperationDao
import dc.stashguard.data.local.OperationEntity
import dc.stashguard.model.Account
import dc.stashguard.model.Category
import dc.stashguard.model.OperationType
import dc.stashguard.model.toAccount
import dc.stashguard.model.toCategory
import dc.stashguard.model.toOperation
import dc.stashguard.screens.operations.add_operation.OperationState
import dc.stashguard.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditOperationViewModel(
    private val accountDao: AccountDao,
    private val operationDao: OperationDao,
    private val categoryDao: CategoryDao,
    private val operationId: String
) : ViewModel() {

    private val _state = MutableStateFlow(OperationState())
    val state: StateFlow<OperationState> = _state.asStateFlow()

    private val originalOperation = MutableStateFlow<OperationEntity?>(null)

    val availableAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
        .map { entities -> entities.map { it.toAccount() } }

    val availableCategories: Flow<List<Category>> = combine(
        categoryDao.getAllCategories(),
        originalOperation
    ) { categories, operation ->
        if (operation == null) return@combine emptyList()
        val filterType = when (operation.type) {
            OperationType.REVENUE -> CategoryType.REVENUE
            OperationType.EXPENSE -> CategoryType.EXPENSE
            OperationType.TRANSFER -> CategoryType.BOTH
        }
        categories.filter { entity ->
            entity.type == filterType || entity.type == CategoryType.BOTH
        }.map { it.toCategory() }
    }

    init {
        loadOperation()
    }

    private fun loadOperation() {
        viewModelScope.launch {
            try {
                val operationEntity = operationDao.getOperationById(operationId)

                val operation = operationEntity?.toOperation()
                if (operationEntity != null) {
                    originalOperation.value = operationEntity
                    _state.value = OperationState(
                        amount = operationEntity.amount.toString(),
                        categoryId = operationEntity.category,
                        toAccountId = operationEntity.toAccountId ?: "",
                        date = operation?.date ?: DateUtils.currentDate(),
                        note = operationEntity.note,
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
                val oldAmount = originalOp.amount
                val oldToAccountId = originalOp.toAccountId
                val newToAccountId =
                    if (originalOp.type == OperationType.TRANSFER) currentState.toAccountId else null

                val updatedOperation = originalOp.copy(
                    amount = amount,
                    category = currentState.categoryId,
                    date = currentState.date.toString(),
                    note = currentState.note.ifBlank { "" }
                )

                if (originalOp.type == OperationType.TRANSFER) {
                    val linkedOp =
                        originalOp.linkedOperationId?.let { operationDao.getLinkedOperations(it) }
                            ?.firstOrNull { it.accountId == originalOp.toAccountId && it.linkedOperationId == originalOp.linkedOperationId }

                    if (linkedOp != null) {
                        val updatedLinkedOp = linkedOp.copy(
                            amount = amount,
                            date = updatedOperation.date,
                            note = updatedOperation.note,
                            toAccountId = originalOp.accountId
                        )

                        operationDao.updateOperations(listOf(updatedOperation, updatedLinkedOp))
                    } else {
                        _state.update { it.copy(error = "Linked transfer operation not found") }
                        return@launch
                    }
                } else {
                    operationDao.updateOperation(updatedOperation)
                }

                val fromAccountId = originalOp.accountId
                val balanceChange =
                    amount - oldAmount

                if (originalOp.type == OperationType.REVENUE) {
                    updateAccountBalance(fromAccountId, balanceChange)
                } else if (originalOp.type == OperationType.EXPENSE) {
                    updateAccountBalance(fromAccountId, -balanceChange)
                } else if (originalOp.type == OperationType.TRANSFER) {
                    updateAccountBalance(fromAccountId, oldAmount)
                    if (oldToAccountId != null) {
                        updateAccountBalance(oldToAccountId, -oldAmount)
                    }
                    updateAccountBalance(fromAccountId, -amount)
                    if (newToAccountId != null) {
                        updateAccountBalance(newToAccountId, amount)
                    }
                }

                onSuccess() // Notify the UI to navigate back
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error saving operation: ${e.message}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun updateAccountBalance(accountId: String, amountChange: Double) {
        val account = accountDao.getAccountByIdOnce(accountId)
        account?.let {
            val newBalance = it.balance + amountChange
            accountDao.updateAccount(it.copy(balance = newBalance))
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun getOperationType(): OperationType? {
        return originalOperation.value?.type
    }
}