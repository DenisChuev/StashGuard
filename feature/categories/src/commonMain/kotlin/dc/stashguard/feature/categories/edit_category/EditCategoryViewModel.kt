package dc.stashguard.feature.categories.edit_category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.isTransfer
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.repository.OperationRepository
import dc.stashguard.core.domain.usecase.CategoryInUseException
import dc.stashguard.core.domain.usecase.DeleteCategoryUseCase
import dc.stashguard.core.ui.color
import dc.stashguard.feature.categories.add_category.CategoryFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val logger = Logger.withTag("EditCategoryViewModel")

class EditCategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val operationRepository: OperationRepository,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val categoryId: String
) : ViewModel() {

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _form = MutableStateFlow(CategoryFormState())
    val form: StateFlow<CategoryFormState> = _form.asStateFlow()

    /** Operations using this category: while there are any, its type is fixed and it can't be deleted. */
    val operationCount: StateFlow<Int> = operationRepository.observeOperationCountByCategory(categoryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Loaded once, so later database changes don't overwrite the user's edits
        viewModelScope.launch {
            try {
                val loaded = categoryRepository.getCategory(categoryId)
                _category.value = loaded
                loaded?.let {
                    _form.value = CategoryFormState(
                        name = it.name,
                        type = it.type,
                        color = it.color,
                        iconName = it.iconName
                    )
                }
            } catch (e: Exception) {
                logger.e(e) { "Error loading category" }
                _error.value = "Error loading category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Editing the name clears a validation error about it
    fun updateName(name: String) {
        _form.update { it.copy(name = name) }
        _error.value = null
    }

    fun updateType(type: CategoryType) {
        _form.update { it.copy(type = type) }
    }

    fun updateColor(color: Color) {
        _form.update { it.copy(color = color) }
    }

    fun updateIcon(iconName: String) {
        _form.update { it.copy(iconName = iconName) }
    }

    /** Validates and saves the edits, then calls [onSuccess] once they are in the database. */
    fun saveEditedCategory(onSuccess: () -> Unit) {
        // Ignore repeated taps while the first save is running
        if (_isSaving.value) return

        val currentCategory = _category.value
        if (currentCategory == null || currentCategory.isTransfer) {
            _error.value = "This category can't be edited."
            return
        }

        val form = _form.value
        val name = form.name.trim()
        if (name.isBlank()) {
            _error.value = "Category name is required."
            return
        }

        _isSaving.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                // Its operations would no longer match it, e.g. an expense in a revenue category
                if (form.type != currentCategory.type &&
                    operationRepository.observeOperationCountByCategory(categoryId).first() > 0
                ) {
                    _error.value = "The type of a category with operations can't change."
                    _isSaving.value = false
                    return@launch
                }

                categoryRepository.updateCategory(
                    currentCategory.copy(
                        name = name,
                        colorArgb = form.color.toArgb(),
                        iconName = form.iconName,
                        type = form.type
                    )
                )
                // isSaving stays true so the button is not tappable while the screen closes
                onSuccess()
            } catch (e: Exception) {
                logger.e(e) { "Error saving category" }
                _error.value = "Error saving category: ${e.message}"
                _isSaving.value = false
            }
        }
    }

    fun deleteCategory(onSuccess: () -> Unit) {
        logger.d("Trying to delete category: $categoryId")
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(categoryId)
                onSuccess()
            } catch (e: CategoryInUseException) {
                _error.value = "Can't delete: used by ${operationCountText(e.operationCount)}."
            } catch (e: Exception) {
                logger.e(e) { "Error deleting category" }
                _error.value = "Error deleting category: ${e.message}"
            }
        }
    }
}

internal fun operationCountText(count: Int): String =
    if (count == 1) "1 operation" else "$count operations"
