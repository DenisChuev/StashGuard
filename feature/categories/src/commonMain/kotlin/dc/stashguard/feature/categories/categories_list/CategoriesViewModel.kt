package dc.stashguard.feature.categories.categories_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// The default categories are seeded at app startup (see initKoin), not here.
class CategoriesViewModel(categoryRepository: CategoryRepository) : ViewModel() {
    /** Null until the first database read, so the empty state doesn't flash while loading. */
    val categories: StateFlow<List<Category>?> = categoryRepository.observeCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}
