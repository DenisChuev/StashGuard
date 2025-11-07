@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package dc.stashguard.screens.operations.add_operation

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
import dc.stashguard.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
    private val accountDao: AccountDao,
    private val operationDao: OperationDao,
    private val categoryDao: CategoryDao,
    private val accountId: String,
    private val operationType: OperationType
) : ViewModel() {

    private val _state = MutableStateFlow(OperationState())
    val state: StateFlow<OperationState> = _state.asStateFlow()

    val availableAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
        .map { entities -> entities.map { it.toAccount() } }

    val availableCategories: Flow<List<Category>> = categoryDao.getCategoriesByType(
        when (operationType) {
            OperationType.REVENUE -> CategoryType.REVENUE
            OperationType.EXPENSE -> CategoryType.EXPENSE
            OperationType.TRANSFER -> CategoryType.BOTH
        }
    ).map { entities -> entities.map { it.toCategory() } }


    // For transfers, automatically select the transfer category
    init {
        if (operationType == OperationType.TRANSFER) {
            viewModelScope.launch {
                // Find and select the transfer category
                val transferCategory = categoryDao.getCategoriesByType(CategoryType.BOTH)
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
                when (operationType) {
                    OperationType.REVENUE -> saveRevenueOperation(amount, currentState)
                    OperationType.EXPENSE -> saveExpenseOperation(amount, currentState)
                    OperationType.TRANSFER -> saveTransferOperation(
                        amount,
                        currentState
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error saving operation: ${e.message}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun saveRevenueOperation(amount: Double, state: OperationState) {
        val operation = OperationEntity(
            accountId = accountId,
            type = OperationType.REVENUE,
            amount = amount,
            category = state.categoryId,
            date = state.date.toString(),
            note = state.note
        )
        operationDao.insertOperation(operation)
        updateAccountBalance(amount, isRevenue = true)
    }

    private suspend fun saveExpenseOperation(amount: Double, state: OperationState) {
        val operation = OperationEntity(
            accountId = accountId,
            type = OperationType.EXPENSE,
            amount = amount,
            category = state.categoryId,
            date = state.date.toString(),
            note = state.note
        )
        operationDao.insertOperation(operation)
        updateAccountBalance(amount, isRevenue = false)
    }

    private suspend fun saveTransferOperation(amount: Double, state: OperationState) {
        val transferId = Uuid.random().toString()
        val currentTime = DateUtils.currentDateMillis()

        val expenseOperation = OperationEntity(
            accountId = accountId,
            type = OperationType.TRANSFER,
            amount = amount,
            category = "Transfer",
            date = state.date.toString(),
            note = state.note,
            createdAt = currentTime,
            linkedOperationId = transferId,
            toAccountId = state.toAccountId
        )

        val revenueOperation = OperationEntity(
            accountId = state.toAccountId,
            type = OperationType.TRANSFER,
            amount = amount,
            category = "Transfer",
            date = state.date.toString(),
            note = state.note,
            createdAt = currentTime,
            linkedOperationId = transferId,
            toAccountId = accountId
        )

        operationDao.insertOperations(listOf(expenseOperation, revenueOperation))
        updateTransferAccountBalances(amount, state.toAccountId)
    }

    private suspend fun updateAccountBalance(amount: Double, isRevenue: Boolean) {
        val account = accountDao.getAccountByIdOnce(accountId)
        account?.let {
            val newBalance = if (isRevenue) it.balance + amount else it.balance - amount
            accountDao.updateAccount(it.copy(balance = newBalance))
        }
    }

    private suspend fun updateTransferAccountBalances(amount: Double, toAccountId: String) {
        val fromAccount = accountDao.getAccountByIdOnce(accountId)
        val toAccount = accountDao.getAccountByIdOnce(toAccountId)

        fromAccount?.let {
            accountDao.updateAccount(it.copy(balance = it.balance - amount))
        }
        toAccount?.let {
            accountDao.updateAccount(it.copy(balance = it.balance + amount))
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}