package dc.stashguard.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

enum class BottomNavigationItem(
    val tab: NavKey,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accent: Color,
    val motion: NavIconMotion
) {
    Accounts(
        tab = AccountsTab,
        title = "Accounts",
        selectedIcon = Icons.Filled.AccountBalance,
        unselectedIcon = Icons.Outlined.AccountBalance,
        accent = Color(0xFF00796B),
        motion = NavIconMotion.Lift
    ),
    Operations(
        tab = OperationsTab,
        title = "History",
        selectedIcon = Icons.Filled.Receipt,
        unselectedIcon = Icons.Outlined.Receipt,
        accent = Color(0xFF1565C0),
        motion = NavIconMotion.Wiggle
    ),
    Categories(
        tab = CategoriesTab,
        title = "Categories",
        selectedIcon = Icons.Filled.Category,
        unselectedIcon = Icons.Outlined.Category,
        accent = Color(0xFFD84315),
        motion = NavIconMotion.Spin
    ),
}

/** The extra movement an icon makes when its tab gets selected. */
enum class NavIconMotion { Lift, Wiggle, Spin }
