package dc.stashguard.core.testing

import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [CategoryRepository] that behaves like the Room one. */
class FakeCategoryRepository(vararg initial: Category) : CategoryRepository {
    private val categories = MutableStateFlow(initial.toList())

    /** Every category, in insertion order. */
    val all: List<Category> get() = categories.value

    override fun observeCategories(): Flow<List<Category>> = categories.map { it.sortedBy { c -> c.name } }

    override fun observeCategoriesByType(type: CategoryType): Flow<List<Category>> =
        observeCategories().map { list -> list.filter { it.type == type || it.type == CategoryType.BOTH } }

    override suspend fun getCategory(id: String): Category? = categories.value.firstOrNull { it.id == id }

    override suspend fun addCategories(categories: List<Category>) {
        val ids = categories.map { it.id }.toSet()
        this.categories.update { list -> list.filterNot { it.id in ids } + categories }
    }

    override suspend fun updateCategory(category: Category) {
        categories.update { list -> list.map { if (it.id == category.id) category else it } }
    }

    override suspend fun deleteCategory(id: String) {
        categories.update { list -> list.filterNot { it.id == id } }
    }
}
