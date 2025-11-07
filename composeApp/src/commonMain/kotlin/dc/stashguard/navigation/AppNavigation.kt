package dc.stashguard.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import dc.stashguard.screens.accounts.accounts_list.AccountsScreen
import dc.stashguard.screens.accounts.add_account.AddAccountScreen
import dc.stashguard.screens.accounts.details.DetailsAccountScreen
import dc.stashguard.screens.accounts.edit_account.EditAccountScreen
import dc.stashguard.screens.categories.CategoriesScreen
import dc.stashguard.screens.operations.OperationsScreen
import dc.stashguard.screens.operations.add_operation.AddOperationScreen

val mainTabRoutes = setOf(
    AccountsTab.ROUTE,
    OperationsTab.ROUTE,
    CategoriesTab.ROUTE
)

fun isMainTabRoute(route: String?): Boolean {
    return route in mainTabRoutes
}

private val logger = Logger.withTag("AppNavigation")

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Track current route to highlight correct bottom nav item
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Check if current route is a main tab (should show bottom nav)
    val shouldShowBottomBar = remember(currentRoute) {
        isMainTabRoute(currentRoute)
    }

    logger.d("current route: $currentRoute")

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomNavigationItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    when (item) {
                                        is BottomNavigationItem.Accounts -> {
                                            navController.navigateTo(AccountsTab)
                                        }

                                        is BottomNavigationItem.Operations -> {
                                            navController.navigateTo(OperationsTab)
                                        }

                                        BottomNavigationItem.Categories -> {
                                            navController.navigateTo(CategoriesTab)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AccountsTab,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Accounts Tab
            composable<AccountsTab> {
                AccountsScreen(
                    onNavigateToAccountDetails = { accountId ->
                        navController.navigate(DetailsAccount(accountId))
                    },
                    onNavigateToEditAccount = { accountId ->
                        navController.navigate(EditAccount(accountId))
                    },
                    onNavigateToAddAccount = {
                        navController.navigate(AddAccount)
                    }
                )
            }

            // Operations Tab
            composable<OperationsTab> {
                OperationsScreen(
                    onNavigateBack = { navController.navigateTo(AccountsTab) }
                )
            }

            // Nested navigation for Accounts tab
            composable<EditAccount> { backStackEntry ->
                val editAccount = backStackEntry.toRoute<EditAccount>()
                EditAccountScreen(
                    accountId = editAccount.accountId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<DetailsAccount> { backStackEntry ->
                val detailsAccount = backStackEntry.toRoute<DetailsAccount>()
                DetailsAccountScreen(
                    accountId = detailsAccount.accountId,
                    onNavigateToEditAccount = { accountId ->
                        navController.navigate(EditAccount(accountId))
                    },
                    onNavigateAddOperation = { accountId, operationType ->
                        navController.navigate(AddOperation(accountId, operationType))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<AddAccount> {
                AddAccountScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<AddOperation> { backStackEntry ->
                val addOperation = backStackEntry.toRoute<AddOperation>()
                AddOperationScreen(
                    accountId = addOperation.accountId,
                    operationType = addOperation.operationType,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<CategoriesTab> {
                CategoriesScreen(
                    onNavigateBack = { /* Not needed for tab navigation */ }
                )
            }
        }
    }
}

fun NavController.navigateTo(route: Any) {
    this.navigate(route = route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}