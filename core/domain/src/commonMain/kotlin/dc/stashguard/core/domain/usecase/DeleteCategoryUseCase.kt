package dc.stashguard.core.domain.usecase

import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.coroutines.flow.first

/** Thrown when deleting a category that operations still use. */
class CategoryInUseException(val operationCount: Int) :
    IllegalStateException("Category is used by $operationCount operations")

/**
 * Deletes a category that no operation uses, so no operation is left pointing at a missing
 * category. The Transfer category can never be deleted.
 */
class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository,
    private val operationRepository: OperationRepository,
) {
    suspend operator fun invoke(categoryId: String) {
        require(categoryId != TRANSFER_CATEGORY_ID) { "The Transfer category can't be deleted" }
        val operationCount = operationRepository.observeOperationCountByCategory(categoryId).first()
        if (operationCount > 0) throw CategoryInUseException(operationCount)
        categoryRepository.deleteCategory(categoryId)
    }
}
