package dc.stashguard.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.data.local.CategoryDao
import dc.stashguard.data.local.CategoryEntity
import dc.stashguard.data.local.CategoryType
import dc.stashguard.model.Category
import dc.stashguard.model.toCategory
import dc.stashguard.model.toCategoryEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val _categories = categoryDao.getAllCategories()
        .map { entities -> entities.map { it.toCategory() } }
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
        initializeDefaultCategories()
    }

    private fun initializeDefaultCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Check if categories table is empty
                val existingCategories = categoryDao.getAllCategories().first()
                if (existingCategories.isEmpty()) {
                    // Add default categories
                    addDefaultCategories()
                }
            } catch (e: Exception) {
                _error.value = "Error initializing categories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun addDefaultCategories() {
        val defaultCategories = listOf(
            // Expense Categories
            CategoryEntity(
                id = "category_food",
                name = "Food",
                color = 0xFFF44336.toInt(),
                iconName = "restaurant",
                type = CategoryType.EXPENSE
            ),
            CategoryEntity(
                id = "category_transport",
                name = "Transport",
                color = 0xFF2196F3.toInt(),
                iconName = "directions_car",
                type = CategoryType.EXPENSE
            ),
            CategoryEntity(
                id = "category_shopping",
                name = "Shopping",
                color = 0xFF9C27B0.toInt(),
                iconName = "shopping_cart",
                type = CategoryType.EXPENSE
            ),
            CategoryEntity(
                id = "category_entertainment",
                name = "Entertainment",
                color = 0xFFFF9800.toInt(),
                iconName = "movie",
                type = CategoryType.EXPENSE
            ),
            CategoryEntity(
                id = "category_healthcare",
                name = "Healthcare",
                color = 0xFF4CAF50.toInt(),
                iconName = "local_hospital",
                type = CategoryType.EXPENSE
            ),
            CategoryEntity(
                id = "category_bills",
                name = "Bills",
                color = 0xFF607D8B.toInt(),
                iconName = "receipt",
                type = CategoryType.EXPENSE
            ),
            CategoryEntity(
                id = "category_education",
                name = "Education",
                color = 0xFF795548.toInt(),
                iconName = "school",
                type = CategoryType.EXPENSE
            ),

            // Revenue Categories
            CategoryEntity(
                id = "category_salary",
                name = "Salary",
                color = 0xFF4CAF50.toInt(),
                iconName = "work",
                type = CategoryType.REVENUE
            ),
            CategoryEntity(
                id = "category_freelance",
                name = "Freelance",
                color = 0xFF2196F3.toInt(),
                iconName = "computer",
                type = CategoryType.REVENUE
            ),
            CategoryEntity(
                id = "category_investment",
                name = "Investment",
                color = 0xFFFFC107.toInt(),
                iconName = "trending_up",
                type = CategoryType.REVENUE
            ),
            CategoryEntity(
                id = "category_gift",
                name = "Gift",
                color = 0xFFE91E63.toInt(),
                iconName = "card_giftcard",
                type = CategoryType.REVENUE
            ),

            // Both Categories (for transfers)
            CategoryEntity(
                id = "category_transfer",
                name = "Transfer",
                color = 0xFF9E9E9E.toInt(),
                iconName = "swap_horiz",
                type = CategoryType.BOTH
            )
        )

        categoryDao.insertCategories(defaultCategories)
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
                categoryDao.insertCategory(category.toCategoryEntity())
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error adding category: ${e.message}"
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryDao.updateCategory(category.toCategoryEntity())
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error updating category: ${e.message}"
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                categoryDao.deleteCategoryById(categoryId)
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