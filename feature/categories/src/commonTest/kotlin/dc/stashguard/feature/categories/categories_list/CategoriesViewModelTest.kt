package dc.stashguard.feature.categories.categories_list

import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import dc.stashguard.core.testing.FakeCategoryRepository
import dc.stashguard.core.testing.MainDispatcherTest
import dc.stashguard.core.testing.category
import dc.stashguard.core.testing.keepCollecting
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CategoriesViewModelTest : MainDispatcherTest() {
    private val categories = FakeCategoryRepository(
        category("food", CategoryType.EXPENSE),
        category(TRANSFER_CATEGORY_ID, CategoryType.BOTH),
    )

    @Test
    fun theBuiltInTransferCategoryIsNotListed() = runTest {
        val viewModel = CategoriesViewModel(categories)
        keepCollecting(viewModel.categories)

        assertEquals(listOf("food"), viewModel.categories.value?.map { it.id })
    }
}
