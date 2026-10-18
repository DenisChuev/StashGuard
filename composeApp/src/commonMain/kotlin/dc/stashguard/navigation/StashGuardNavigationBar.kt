package dc.stashguard.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey

private const val SELECTED_ICON_SCALE = 1.15f
private val LIFT_HEIGHT = 4.dp

@Composable
fun StashGuardNavigationBar(
    selectedTab: NavKey,
    onTabClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    // No pressed/hovered/focused highlight: the selected tab is shown by its color and icon alone
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        NavigationBarItems(selectedTab, onTabClick, modifier)
    }
}

@Composable
private fun NavigationBarItems(
    selectedTab: NavKey,
    onTabClick: (NavKey) -> Unit,
    modifier: Modifier
) {
    NavigationBar(modifier = modifier) {
        BottomNavigationItem.entries.forEach { item ->
            val selected = selectedTab == item.tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabClick(item.tab) },
                icon = { AnimatedNavIcon(item = item, selected = selected) },
                label = {
                    Text(
                        text = item.title,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = item.accent,
                    selectedTextColor = item.accent,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun AnimatedNavIcon(item: BottomNavigationItem, selected: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (selected) SELECTED_ICON_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    // Lift: fraction of LIFT_HEIGHT; Wiggle and Spin: rotation in degrees
    val twist = remember { Animatable(0f) }
    // The bar is recreated whenever it reappears at a tab root, so don't replay the twist then
    var isFirstRun by remember { mutableStateOf(true) }

    LaunchedEffect(selected) {
        if (isFirstRun) {
            isFirstRun = false
            return@LaunchedEffect
        }
        if (!selected) return@LaunchedEffect
        twist.snapTo(0f)
        when (item.motion) {
            NavIconMotion.Lift -> twist.animateTo(0f, keyframes {
                durationMillis = 350
                1f at 120 using FastOutSlowInEasing
            })
            NavIconMotion.Wiggle -> twist.animateTo(0f, keyframes {
                durationMillis = 400
                -12f at 80
                12f at 200
                -6f at 300
            })
            NavIconMotion.Spin -> {
                twist.animateTo(360f, tween(durationMillis = 450, easing = FastOutSlowInEasing))
                twist.snapTo(0f)
            }
        }
    }

    Crossfade(
        targetState = selected,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            if (item.motion == NavIconMotion.Lift) {
                translationY = -twist.value * LIFT_HEIGHT.toPx()
            } else {
                rotationZ = twist.value
            }
        }
    ) { isSelected ->
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title
        )
    }
}
