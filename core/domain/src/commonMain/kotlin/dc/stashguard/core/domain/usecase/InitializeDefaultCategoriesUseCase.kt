package dc.stashguard.core.domain.usecase

import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first

/** Seeds the default categories when the user has none yet. */
class InitializeDefaultCategoriesUseCase(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke() {
        if (categoryRepository.observeCategories().first().isEmpty()) {
            categoryRepository.addCategories(defaultCategories)
        }
    }

    private val defaultCategories = listOf(
        // Expense Categories
        Category(
            id = "category_food",
            name = "Food",
            colorArgb = 0xFFF44336.toInt(),
            iconName = "restaurant",
            type = CategoryType.EXPENSE
        ),
        Category(
            id = "category_transport",
            name = "Transport",
            colorArgb = 0xFF2196F3.toInt(),
            iconName = "directions_car",
            type = CategoryType.EXPENSE
        ),
        Category(
            id = "category_shopping",
            name = "Shopping",
            colorArgb = 0xFF9C27B0.toInt(),
            iconName = "shopping_cart",
            type = CategoryType.EXPENSE
        ),
        Category(
            id = "category_entertainment",
            name = "Entertainment",
            colorArgb = 0xFFFF9800.toInt(),
            iconName = "movie",
            type = CategoryType.EXPENSE
        ),
        Category(
            id = "category_healthcare",
            name = "Healthcare",
            colorArgb = 0xFF4CAF50.toInt(),
            iconName = "local_hospital",
            type = CategoryType.EXPENSE
        ),
        Category(
            id = "category_bills",
            name = "Bills",
            colorArgb = 0xFF607D8B.toInt(),
            iconName = "receipt",
            type = CategoryType.EXPENSE
        ),
        Category(
            id = "category_education",
            name = "Education",
            colorArgb = 0xFF795548.toInt(),
            iconName = "school",
            type = CategoryType.EXPENSE
        ),

        // Revenue Categories
        Category(
            id = "category_salary",
            name = "Salary",
            colorArgb = 0xFF4CAF50.toInt(),
            iconName = "work",
            type = CategoryType.REVENUE
        ),
        Category(
            id = "category_freelance",
            name = "Freelance",
            colorArgb = 0xFF2196F3.toInt(),
            iconName = "computer",
            type = CategoryType.REVENUE
        ),
        Category(
            id = "category_investment",
            name = "Investment",
            colorArgb = 0xFFFFC107.toInt(),
            iconName = "trending_up",
            type = CategoryType.REVENUE
        ),
        Category(
            id = "category_gift",
            name = "Gift",
            colorArgb = 0xFFE91E63.toInt(),
            iconName = "card_giftcard",
            type = CategoryType.REVENUE
        ),

        // Both Categories (for transfers)
        Category(
            id = "category_transfer",
            name = "Transfer",
            colorArgb = 0xFF9E9E9E.toInt(),
            iconName = "swap_horiz",
            type = CategoryType.BOTH
        )
    )
}
