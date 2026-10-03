package dc.stashguard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

// Back stacks are saved with kotlinx.serialization. iOS has no reflection,
// so every route has to be registered here.
private val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AccountsTab::class, AccountsTab.serializer())
            subclass(OperationsTab::class, OperationsTab.serializer())
            subclass(CategoriesTab::class, CategoriesTab.serializer())
            subclass(EditAccount::class, EditAccount.serializer())
            subclass(AddAccount::class, AddAccount.serializer())
            subclass(DetailsAccount::class, DetailsAccount.serializer())
            subclass(AddOperation::class, AddOperation.serializer())
            subclass(EditOperation::class, EditOperation.serializer())
        }
    }
}

/**
 * One back stack per bottom-bar tab, so switching tabs keeps each tab's screens.
 * The first tab is the start tab: back from the root of any other tab returns to it.
 */
class NavigationState(
    val tabs: List<NavKey>,
    private val selectedTabIndex: MutableIntState,
    private val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    private val startTab: NavKey get() = tabs.first()

    val selectedTab: NavKey get() = tabs[selectedTabIndex.intValue]

    private val currentStack: NavBackStack<NavKey> get() = backStacks.getValue(selectedTab)

    /** True when a tab's root screen is shown, i.e. the bottom bar should be visible. */
    val isAtTabRoot: Boolean get() = currentStack.size == 1

    /** Tabs whose entries are shown, bottom to top. */
    val stacksInUse: List<NavKey>
        get() = if (selectedTab == startTab) listOf(startTab) else listOf(startTab, selectedTab)

    /** Switches to [key] if it is a tab, otherwise pushes it onto the current tab's stack. */
    fun navigate(key: NavKey) {
        val tabIndex = tabs.indexOf(key)
        if (tabIndex != -1) {
            selectedTabIndex.intValue = tabIndex
        } else {
            currentStack.add(key)
        }
    }

    fun goBack() {
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
        } else if (selectedTab != startTab) {
            selectedTabIndex.intValue = 0
        }
    }

    @Composable
    fun rememberEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        // Each stack is decorated separately so a hidden tab keeps its ViewModels and saved state.
        val decoratedStacks = tabs.associateWith { tab ->
            rememberDecoratedNavEntries(
                backStack = backStacks.getValue(tab),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    // Gives every entry its own ViewModelStore, so koinViewModel { parametersOf(id) }
                    // creates a ViewModel per screen instead of sharing one across the app
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
            )
        }
        return stacksInUse.flatMap { decoratedStacks.getValue(it) }
    }
}

@Composable
fun rememberNavigationState(tabs: List<NavKey>): NavigationState {
    val selectedTabIndex = rememberSaveable { mutableIntStateOf(0) }
    val backStacks = tabs.associateWith { tab -> rememberNavBackStack(navSavedStateConfig, tab) }
    return NavigationState(tabs, selectedTabIndex, backStacks)
}
