package dc.stashguard.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import co.touchlab.kermit.Logger
import dc.stashguard.feature.accounts.accounts_list.AccountsScreen
import dc.stashguard.feature.accounts.add_account.AddAccountScreen
import dc.stashguard.feature.accounts.details.DetailsAccountScreen
import dc.stashguard.feature.accounts.edit_account.EditAccountScreen
import dc.stashguard.feature.categories.add_category.AddCategoryScreen
import dc.stashguard.feature.categories.categories_list.CategoriesScreen
import dc.stashguard.feature.categories.edit_category.EditCategoryScreen
import dc.stashguard.feature.operations.OperationsScreen
import dc.stashguard.feature.operations.add_operation.AddOperationScreen
import dc.stashguard.feature.operations.edit_operation.EditOperationScreen

private val logger = Logger.withTag("AppNavigation")

private const val NAV_TRANSITION_MILLIS = 100

private val navTransition =
    fadeIn(tween(NAV_TRANSITION_MILLIS)) togetherWith fadeOut(tween(NAV_TRANSITION_MILLIS))

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navigationState = rememberNavigationState(
        tabs = BottomNavigationItem.entries.map { it.tab }
    )

    logger.d("selected tab: ${navigationState.selectedTab}")

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (navigationState.isAtTabRoot) {
                StashGuardNavigationBar(
                    selectedTab = navigationState.selectedTab,
                    onTabClick = navigationState::navigate
                )
            }
        },
    ) { paddingValues ->
        val entryProvider = entryProvider {
            // Accounts Tab
            entry<AccountsTab> {
                AccountsScreen(
                    onNavigateToAccountDetails = { accountId ->
                        navigationState.navigate(DetailsAccount(accountId))
                    },
                    onNavigateToEditAccount = { accountId ->
                        navigationState.navigate(EditAccount(accountId))
                    },
                    onNavigateToAddAccount = {
                        navigationState.navigate(AddAccount)
                    }
                )
            }

            entry<EditAccount> { key ->
                EditAccountScreen(
                    accountId = key.accountId,
                    onNavigateBack = navigationState::goBack,
                    // Screens below (e.g. the account's details) show a deleted account
                    onAccountDeleted = navigationState::goBackToRoot
                )
            }

            entry<DetailsAccount> { key ->
                DetailsAccountScreen(
                    accountId = key.accountId,
                    onNavigateToEditAccount = { accountId ->
                        navigationState.navigate(EditAccount(accountId))
                    },
                    onNavigateAddOperation = { accountId, operationType ->
                        navigationState.navigate(AddOperation(accountId, operationType))
                    },
                    onNavigateToEditOperation = { accountId, operationId, operationType ->
                        navigationState.navigate(EditOperation(accountId, operationId, operationType))
                    },
                    onNavigateBack = navigationState::goBack
                )
            }

            entry<AddAccount> {
                AddAccountScreen(
                    onNavigateBack = navigationState::goBack
                )
            }

            // Operations Tab
            entry<OperationsTab> {
                OperationsScreen(
                    onEditOperation = { accountId, operationId, operationType ->
                        navigationState.navigate(EditOperation(accountId, operationId, operationType))
                    },
                    onNavigateBack = { navigationState.navigate(AccountsTab) },
                    onNavigateToAccounts = { navigationState.navigate(AccountsTab) }
                )
            }

            entry<AddOperation> { key ->
                AddOperationScreen(
                    accountId = key.accountId,
                    operationType = key.operationType,
                    onNavigateBack = navigationState::goBack
                )
            }

            entry<EditOperation> { key ->
                EditOperationScreen(
                    operationId = key.operationId,
                    onNavigateBack = navigationState::goBack
                )
            }

            // Categories Tab
            entry<CategoriesTab> {
                CategoriesScreen(
                    onNavigateBack = { },
                    onNavigateToAddCategory = {
                        navigationState.navigate(AddCategory)
                    },
                    onNavigateToEditCategory = { categoryId ->
                        navigationState.navigate(EditCategory(categoryId))
                    }
                )
            }

            entry<AddCategory> {
                AddCategoryScreen(
                    onNavigateBack = navigationState::goBack
                )
            }

            entry<EditCategory> { key ->
                EditCategoryScreen(
                    categoryId = key.categoryId,
                    onNavigateBack = navigationState::goBack
                )
            }
        }

        NavDisplay(
            entries = navigationState.rememberEntries(entryProvider),
            onBack = navigationState::goBack,
            // Every navigation is a short crossfade instead of the platform's slide
            transitionSpec = { navTransition },
            popTransitionSpec = { navTransition },
            predictivePopTransitionSpec = { navTransition },
            // Consumed so screens' own top bars and Scaffolds don't add the status bar again
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        )
    }
}
