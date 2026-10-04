package dc.stashguard.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.usecase.InitializeDefaultCategoriesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val initializeDefaultCategories: InitializeDefaultCategoriesUseCase,
) : ViewModel() {

    private val _categories = categoryRepository.observeCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<Category>> = _categories

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadDefaultCategories()
    }

    private fun loadDefaultCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Adds the default categories if the user has none
                initializeDefaultCategories()
            } catch (e: Exception) {
                _error.value = "Error initializing categories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            // The flow will automatically update
            _isLoading.value = false
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.addCategories(listOf(category))
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error adding category: ${e.message}"
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.updateCategory(category)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error updating category: ${e.message}"
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(categoryId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error deleting category: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}