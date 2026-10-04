@file:OptIn(ExperimentalTime::class)

package dc.stashguard.feature.operations.add_operation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.categoryType
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.usecase.AddOperationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class OperationState(
    val amount: String = "",
    val categoryId: String = "",
    val toAccountId: String = "",
    val date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val note: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)


class AddOperationViewModel(
    accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val addOperationUseCase: AddOperationUseCase,
    private val accountId: String,
    private val operationType: OperationType
) : ViewModel() {

    private val _state = MutableStateFlow(OperationState())
    val state: StateFlow<OperationState> = _state.asStateFlow()

    val availableAccounts: Flow<List<Account>> = accountRepository.observeAccounts()

    val availableCategories: Flow<List<Category>> =
        categoryRepository.observeCategoriesByType(operationType.categoryType)


    // For transfers, automatically select the transfer category
    init {
        if (operationType == OperationType.TRANSFER) {
            viewModelScope.launch {
                // Find and select the transfer category
                val transferCategory = categoryRepository.observeCategoriesByType(CategoryType.BOTH)
                    .first()
                    .firstOrNull { it.name == "Transfer" }

                transferCategory?.let {
                    updateCategory(it.id)
                }
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

    fun updateDate(date: LocalDate) {
        _state.update { it.copy(date = date) }
    }

    fun updateNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun saveOperation(onSuccess: () -> Unit) {
        val currentState = _state.value
        val amount = currentState.amount.toDoubleOrNull()

        // Validation
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

            operationType == OperationType.TRANSFER && currentState.toAccountId.isBlank() -> {
                _state.update { it.copy(error = "Please select destination account") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                addOperationUseCase(
                    accountId = accountId,
                    type = operationType,
                    amount = amount,
                    categoryId = currentState.categoryId,
                    date = currentState.date,
                    note = currentState.note,
                    toAccountId = currentState.toAccountId,
                )
                onSuccess()
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
}