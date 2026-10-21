package dc.stashguard.core.domain.usecase

import dc.stashguard.core.testing.FakeCategoryRepository
import dc.stashguard.core.testing.category
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InitializeDefaultCategoriesUseCaseTest {
    private val categories = FakeCategoryRepository()
    private val initializeDefaultCategories = InitializeDefaultCategoriesUseCase(categories)

    @Test
    fun seedsTheDefaultsIncludingTransferWhenThereAreNoCategories() = runTest {
        initializeDefaultCategories()

        assertEquals(12, categories.all.size)
        assertTrue(categories.all.any { it.id == TRANSFER_CATEGORY_ID })
    }

    @Test
    fun runningAgainAddsNothing() = runTest {
        initializeDefaultCategories()
        initializeDefaultCategories()

        assertEquals(12, categories.all.size)
    }

    @Test
    fun userChangesAreNotOverwritten() = runTest {
        initializeDefaultCategories()
        categories.deleteCategory("category_food")
        val renamed = categories.all.first { it.id == "category_salary" }.copy(name = "Paycheck")
        categories.updateCategory(renamed)

        initializeDefaultCategories()

        assertEquals(11, categories.all.size)
        assertEquals("Paycheck", categories.all.first { it.id == "category_salary" }.name)
    }

    @Test
    fun nothingIsSeededWhenTheUserAlreadyHasCategories() = runTest {
        categories.addCategories(listOf(category("custom")))

        initializeDefaultCategories()

        assertEquals(listOf("custom"), categories.all.map { it.id })
    }
}
