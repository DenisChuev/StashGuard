package dc.stashguard.core.domain.repository

import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    /** All categories sorted by name. */
    fun observeCategories(): Flow<List<Category>>

    /** Categories of [type] plus the [CategoryType.BOTH] ones, sorted by name. */
    fun observeCategoriesByType(type: CategoryType): Flow<List<Category>>

    suspend fun addCategories(categories: List<Category>)

    suspend fun updateCategory(category: Category)

    suspend fun deleteCategory(id: String)
}
