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
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dc.stashguard.screens.accounts.edit_account.AccountDetailsScreen
import dc.stashguard.screens.accounts.accounts_list.AccountsScreen
import dc.stashguard.screens.accounts.add_account.AddAccountScreen
import dc.stashguard.screens.operations.OperationsScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Track current route to highlight correct bottom nav item
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
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
                                }
                            }
                        }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AccountsTab,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Accounts Tab
            composable<AccountsTab> {
                AccountsScreen(
                    modifier = modifier,
                    onNavigateToOperations = {
                        navController.navigateTo(OperationsTab)
                    },
                    onNavigateToAccountDetails = { accountId ->
                        navController.navigate(AccountDetails(accountId))
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
            composable<AccountDetails> { backStackEntry ->
                val accountDetails = backStackEntry.toRoute<AccountDetails>()
                AccountDetailsScreen(
                    accountId = accountDetails.accountId,
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