package dc.stashguard.feature.categories.add_category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.ui.DEFAULT_CATEGORY_ICON
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val logger = Logger.withTag("AddCategoryViewModel")

/** The fields of the Add and Edit Category forms. */
data class CategoryFormState(
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val color: Color = DefaultCategoryColor,
    val iconName: String = DEFAULT_CATEGORY_ICON,
)

class AddCategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    private val _form = MutableStateFlow(CategoryFormState())
    val form: StateFlow<CategoryFormState> = _form.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    fun saveCategory(onSuccess: () -> Unit) {
        // Ignore repeated taps while the first insert is running
        if (_isSaving.value) return

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
                val category = Category(
                    name = name,
                    colorArgb = form.color.toArgb(),
                    iconName = form.iconName,
                    type = form.type
                )
                categoryRepository.addCategories(listOf(category))
                // isSaving stays true so the button is not tappable while the screen closes
                onSuccess()
            } catch (e: Exception) {
                logger.e(e) { "Error inserting category" }
                _error.value = "Error saving category: ${e.message}"
                _isSaving.value = false
            }
        }
    }
}
