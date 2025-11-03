package dc.stashguard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dc.stashguard.screens.AccountDetailsScreen
import dc.stashguard.screens.AccountsScreen
import dc.stashguard.screens.AddAccountScreen
import dc.stashguard.screens.OperationsScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Accounts
    ) {
        composable<Accounts> {
            AccountsScreen(
                modifier = modifier,
                onNavigateToOperations = {
                    navController.navigate(Operations)
                },
                onNavigateToAccountDetails = { account ->
                    navController.navigate(AccountDetails(account.name))
                },
                onNavigateToAddAccount = {
                    navController.navigate(AddAccount)
                }
            )
        }

        composable<Operations> {
            OperationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

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
                onAccountAdded = { account ->
                    {

                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}