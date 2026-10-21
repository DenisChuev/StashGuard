package dc.stashguard.core.domain.usecase

import dc.stashguard.core.testing.FakeCategoryRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.category
import dc.stashguard.core.testing.operation
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeleteCategoryUseCaseTest {
    private val categories = FakeCategoryRepository(
        category("category_food"),
        category("category_rent"),
        category(TRANSFER_CATEGORY_ID, CategoryType.BOTH),
    )
    private val operations = FakeOperationRepository()
    private val deleteCategory = DeleteCategoryUseCase(categories, operations)

    @Test
    fun deletesAnUnusedCategory() = runTest {
        deleteCategory("category_rent")

        assertEquals(listOf("category_food", TRANSFER_CATEGORY_ID), categories.all.map { it.id })
    }

    @Test
    fun categoryInUseIsNotDeleted() = runTest {
        operations.addOperations(
            listOf(
                operation("1", "a", OperationType.EXPENSE, 10.0, category = "category_food"),
                operation("2", "a", OperationType.EXPENSE, 20.0, category = "category_food"),
            )
        )

        val error = assertFailsWith<CategoryInUseException> { deleteCategory("category_food") }

        assertEquals(2, error.operationCount)
        assertEquals(3, categories.all.size)
    }

    @Test
    fun transferCategoryIsNeverDeleted() = runTest {
        assertFailsWith<IllegalArgumentException> { deleteCategory(TRANSFER_CATEGORY_ID) }

        assertEquals(3, categories.all.size)
    }
}
