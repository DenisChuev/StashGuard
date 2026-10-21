package dc.stashguard.feature.categories.add_category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.testing.FakeCategoryRepository
import dc.stashguard.core.testing.MainDispatcherTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddCategoryViewModelTest : MainDispatcherTest() {
    private val categories = FakeCategoryRepository()

    @Test
    fun blankNameIsRejected() = runTest {
        val viewModel = AddCategoryViewModel(categories)
        var saved = false
        viewModel.updateName("   ")

        viewModel.saveCategory(onSuccess = { saved = true })

        assertEquals("Category name is required.", viewModel.error.value)
        assertTrue(categories.all.isEmpty())
        assertEquals(false, saved)
    }

    @Test
    fun editingTheNameClearsTheError() = runTest {
        val viewModel = AddCategoryViewModel(categories)
        viewModel.saveCategory(onSuccess = {})

        viewModel.updateName("G")

        assertNull(viewModel.error.value)
    }

    @Test
    fun savesTheCategoryAsEntered() = runTest {
        val viewModel = AddCategoryViewModel(categories)
        var saved = false
        viewModel.updateName("  Groceries ")
        viewModel.updateType(CategoryType.BOTH)
        viewModel.updateColor(Color(0xFF4CAF50))
        viewModel.updateIcon("shopping_cart")

        viewModel.saveCategory(onSuccess = { saved = true })

        val category = categories.all.single()
        assertEquals("Groceries", category.name)
        assertEquals(CategoryType.BOTH, category.type)
        assertEquals(Color(0xFF4CAF50).toArgb(), category.colorArgb)
        assertEquals("shopping_cart", category.iconName)
        assertTrue(saved)
    }

    @Test
    fun repeatedTapsSaveOnce() = runTest {
        val viewModel = AddCategoryViewModel(categories)
        viewModel.updateName("Groceries")

        viewModel.saveCategory(onSuccess = {})
        viewModel.saveCategory(onSuccess = {})

        assertEquals(1, categories.all.size)
    }
}
