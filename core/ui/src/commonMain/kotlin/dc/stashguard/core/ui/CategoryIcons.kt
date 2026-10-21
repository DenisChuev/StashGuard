package dc.stashguard.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import dc.stashguard.core.domain.model.Category

/** Icon used for an unknown icon name, and the default for new categories. */
const val DEFAULT_CATEGORY_ICON = "category"

// Categories store an icon name; these are the icons the user can pick, in picker order.
// The default categories use the first names, so keep them when changing this list.
private val categoryIcons: Map<String, ImageVector> = linkedMapOf(
    "restaurant" to Icons.Default.Restaurant,
    "directions_car" to Icons.Default.DirectionsCar,
    "shopping_cart" to Icons.Default.ShoppingCart,
    "movie" to Icons.Default.Movie,
    "local_hospital" to Icons.Default.LocalHospital,
    "receipt" to Icons.Default.Receipt,
    "school" to Icons.Default.School,
    "work" to Icons.Default.Work,
    "computer" to Icons.Default.Computer,
    "trending_up" to Icons.AutoMirrored.Filled.TrendingUp,
    "card_giftcard" to Icons.Default.CardGiftcard,
    "swap_horiz" to Icons.Default.SwapHoriz,
    "home" to Icons.Default.Home,
    "local_cafe" to Icons.Default.LocalCafe,
    "local_gas_station" to Icons.Default.LocalGasStation,
    "checkroom" to Icons.Default.Checkroom,
    "smartphone" to Icons.Default.Smartphone,
    "flight" to Icons.Default.Flight,
    "fitness_center" to Icons.Default.FitnessCenter,
    "sports_esports" to Icons.Default.SportsEsports,
    "pets" to Icons.Default.Pets,
    "child_care" to Icons.Default.ChildCare,
    "savings" to Icons.Default.Savings,
    "attach_money" to Icons.Default.AttachMoney,
    DEFAULT_CATEGORY_ICON to Icons.Default.Category,
)

/** The icon names the user can pick for a category. */
val categoryIconNames: List<String> = categoryIcons.keys.toList()

fun categoryIcon(iconName: String): ImageVector = categoryIcons[iconName] ?: Icons.Default.Category

val Category.icon: ImageVector get() = categoryIcon(iconName)
