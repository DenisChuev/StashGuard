package dc.stashguard.feature.categories.edit_category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import app.cash.turbine.test
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import dc.stashguard.core.domain.usecase.DeleteCategoryUseCase
import dc.stashguard.core.testing.FakeCategoryRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.MainDispatcherTest
import dc.stashguard.core.testing.category
import dc.stashguard.core.testing.operation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditCategoryViewModelTest : MainDispatcherTest() {
    private val categories = FakeCategoryRepository(
        category("food", CategoryType.EXPENSE),
        category(TRANSFER_CATEGORY_ID, CategoryType.BOTH),
    )
    private val operations = FakeOperationRepository()

    private fun viewModel(categoryId: String = "food") = EditCategoryViewModel(
        categoryRepository = categories,
        operationRepository = operations,
        deleteCategoryUseCase = DeleteCategoryUseCase(categories, operations),
        categoryId = categoryId,
    )

    private suspend fun addFoodOperations(count: Int) = operations.addOperations(
        List(count) { operation("op$it", "a", OperationType.EXPENSE, 10.0, category = "food") }
    )

    @Test
    fun loadsTheCategoryIntoTheForm() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.isLoading.value)
        assertEquals("food", viewModel.form.value.name)
        assertEquals(CategoryType.EXPENSE, viewModel.form.value.type)
    }

    @Test
    fun missingCategoryLoadsAsNull() = runTest {
        val viewModel = viewModel(categoryId = "missing")

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.category.value)
    }

    @Test
    fun savesNameColorAndIcon() = runTest {
        val viewModel = viewModel()
        var saved = false
        viewModel.updateName(" Groceries ")
        viewModel.updateColor(Color(0xFF2196F3))
        viewModel.updateIcon("shopping_cart")

        viewModel.saveEditedCategory(onSuccess = { saved = true })

        val category = categories.all.first { it.id == "food" }
        assertEquals("Groceries", category.name)
        assertEquals(Color(0xFF2196F3).toArgb(), category.colorArgb)
        assertEquals("shopping_cart", category.iconName)
        assertTrue(saved)
    }

    @Test
    fun blankNameIsRejected() = runTest {
        val viewModel = viewModel()
        viewModel.updateName(" ")

        viewModel.saveEditedCategory(onSuccess = {})

        assertEquals("Category name is required.", viewModel.error.value)
        assertEquals("food", categories.all.first { it.id == "food" }.name)
    }

    @Test
    fun typeOfAnUnusedCategoryCanChange() = runTest {
        val viewModel = viewModel()
        viewModel.updateType(CategoryType.REVENUE)

        viewModel.saveEditedCategory(onSuccess = {})

        assertEquals(CategoryType.REVENUE, categories.all.first { it.id == "food" }.type)
    }

    @Test
    fun typeOfACategoryInUseCantChange() = runTest {
        addFoodOperations(1)
        val viewModel = viewModel()
        var saved = false
        viewModel.updateType(CategoryType.REVENUE)

        viewModel.saveEditedCategory(onSuccess = { saved = true })

        assertEquals("The type of a category with operations can't change.", viewModel.error.value)
        assertEquals(CategoryType.EXPENSE, categories.all.first { it.id == "food" }.type)
        assertFalse(saved)
        // The user can fix the type and save again
        assertFalse(viewModel.isSaving.value)
    }

    @Test
    fun operationCountFollowsTheOperations() = runTest {
        addFoodOperations(2)
        val viewModel = viewModel()

        viewModel.operationCount.test {
            assertEquals(2, expectMostRecentItem())
        }
    }

    @Test
    fun deletesAnUnusedCategory() = runTest {
        val viewModel = viewModel()
        var deleted = false

        viewModel.deleteCategory(onSuccess = { deleted = true })

        assertTrue(deleted)
        assertTrue(categories.all.none { it.id == "food" })
    }

    @Test
    fun categoryInUseIsNotDeleted() = runTest {
        addFoodOperations(2)
        val viewModel = viewModel()
        var deleted = false

        viewModel.deleteCategory(onSuccess = { deleted = true })

        assertEquals("Can't delete: used by 2 operations.", viewModel.error.value)
        assertTrue(categories.all.any { it.id == "food" })
        assertFalse(deleted)
    }

    @Test
    fun transferCategoryCantBeEdited() = runTest {
        val viewModel = viewModel(categoryId = TRANSFER_CATEGORY_ID)
        viewModel.updateName("Moves")

        viewModel.saveEditedCategory(onSuccess = {})

        assertEquals("This category can't be edited.", viewModel.error.value)
        assertEquals(TRANSFER_CATEGORY_ID, categories.all.first { it.id == TRANSFER_CATEGORY_ID }.name)
    }
}
