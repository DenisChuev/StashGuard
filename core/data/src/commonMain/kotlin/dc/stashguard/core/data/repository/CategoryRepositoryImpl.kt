package dc.stashguard.core.data.repository

import dc.stashguard.core.data.mapper.toDomain
import dc.stashguard.core.data.mapper.toEntity
import dc.stashguard.core.database.dao.CategoryDao
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { entities -> entities.map { it.toDomain() } }

    override fun observeCategoriesByType(type: CategoryType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type.name).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addCategories(categories: List<Category>) {
        categoryDao.insertCategories(categories.map { it.toEntity() })
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(id: String) {
        categoryDao.deleteCategoryById(id)
    }
}
