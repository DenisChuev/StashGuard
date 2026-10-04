package dc.stashguard.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import co.touchlab.kermit.Logger
import dc.stashguard.feature.accounts.accounts_list.AccountsScreen
import dc.stashguard.feature.accounts.add_account.AddAccountScreen
import dc.stashguard.feature.accounts.details.DetailsAccountScreen
import dc.stashguard.feature.accounts.edit_account.EditAccountScreen
import dc.stashguard.feature.categories.CategoriesScreen
import dc.stashguard.feature.operations.OperationsScreen
import dc.stashguard.feature.operations.add_operation.AddOperationScreen
import dc.stashguard.feature.operations.edit_operation.EditOperationScreen

private val logger = Logger.withTag("AppNavigation")

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
                NavigationBar {
                    BottomNavigationItem.entries.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = navigationState.selectedTab == item.tab,
                            onClick = { navigationState.navigate(item.tab) }
                        )
                    }
                }
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
                    onNavigateBack = navigationState::goBack
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
                    onNavigateBack = { navigationState.navigate(AccountsTab) }
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
                    onNavigateBack = { }
                )
            }
        }

        NavDisplay(
            entries = navigationState.rememberEntries(entryProvider),
            onBack = navigationState::goBack,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
