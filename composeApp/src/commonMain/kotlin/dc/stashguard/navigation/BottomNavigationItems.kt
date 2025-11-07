package dc.stashguard.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Accounts : BottomNavigationItem(
        route = AccountsTab.ROUTE,
        title = AccountsTab.TITLE,
        icon = Icons.Default.AccountBalance
    )

    object Operations : BottomNavigationItem(
        route = OperationsTab.ROUTE,
        title = OperationsTab.TITLE,
        icon = Icons.Default.Receipt
    )

    object Categories : BottomNavigationItem(
        route = "CategoriesTab",
        title = "Categories",
        icon = Icons.Default.Category
    )
}

val bottomNavigationItems = listOf(
    BottomNavigationItem.Accounts,
    BottomNavigationItem.Operations,
    BottomNavigationItem.Categories
)