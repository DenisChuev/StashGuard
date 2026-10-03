package dc.stashguard.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

enum class BottomNavigationItem(
    val tab: NavKey,
    val title: String,
    val icon: ImageVector
) {
    Accounts(tab = AccountsTab, title = "Accounts", icon = Icons.Default.AccountBalance),
    Operations(tab = OperationsTab, title = "Operations", icon = Icons.Default.Receipt),
    Categories(tab = CategoriesTab, title = "Categories", icon = Icons.Default.Category),
}
