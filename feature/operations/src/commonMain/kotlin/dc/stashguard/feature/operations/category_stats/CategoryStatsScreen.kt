@file:OptIn(ExperimentalMaterial3Api::class)

package dc.stashguard.feature.operations.category_stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.core.common.toCurrencyString
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.ui.DEFAULT_CATEGORY_ICON
import dc.stashguard.core.ui.categoryIcon
import dc.stashguard.core.ui.color
import dc.stashguard.core.ui.icon
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

/** The types the screen switches between: transfers only move money, so they have no breakdown. */
private val STATS_TYPES = listOf(OperationType.REVENUE, OperationType.EXPENSE)

private val monthFormat = YearMonth.Format {
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

@Composable
fun CategoryStatsScreen(
    initialType: OperationType,
    onNavigateBack: () -> Unit,
    viewModel: CategoryStatsViewModel = koinViewModel { parametersOf(initialType) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("By category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        // Still loading: show nothing rather than a flash of the empty state
        val stats = state ?: return@Scaffold
        val outline = MaterialTheme.colorScheme.outline

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "type") {
                TypeSelector(selected = stats.type, onSelect = viewModel::selectType)
            }
            item(key = "month") {
                MonthSwitcher(
                    month = stats.month,
                    canGoNext = stats.canGoNext,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth
                )
            }

            if (stats.slices.isEmpty()) {
                item(key = "empty") {
                    EmptyMonth(type = stats.type, month = stats.month)
                }
            } else {
                item(key = "chart") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        DonutChart(
                            slices = stats.slices,
                            colorOf = { it.category?.color ?: outline }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stats.type.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stats.total.toCurrencyString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                items(stats.slices, key = { it.category?.id ?: "uncategorized" }) { slice ->
                    CategorySliceRow(slice = slice, color = slice.category?.color ?: outline)
                }
            }
        }
    }
}

@Composable
private fun TypeSelector(
    selected: OperationType,
    onSelect: (OperationType) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        STATS_TYPES.forEachIndexed { index, type ->
            SegmentedButton(
                selected = type == selected,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = STATS_TYPES.size)
            ) {
                Text(type.label)
            }
        }
    }
}

@Composable
private fun MonthSwitcher(
    month: YearMonth,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month")
        }
        Text(
            text = month.format(monthFormat),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month")
        }
    }
}

@Composable
private fun EmptyMonth(type: OperationType, month: YearMonth) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PieChart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No ${type.label.lowercase()} in ${month.format(monthFormat)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A category's icon and name, its share and its amount, like a history row. */
@Composable
private fun CategorySliceRow(slice: CategorySlice, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = slice.category?.icon ?: categoryIcon(DEFAULT_CATEGORY_ICON),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = slice.category?.name ?: "Uncategorized",
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = slice.amount.toCurrencyString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = slice.fraction.percentText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val OperationType.label: String
    get() = when (this) {
        OperationType.REVENUE -> "Income"
        OperationType.EXPENSE -> "Expenses"
        OperationType.TRANSFER -> "Transfers"
    }

/** "42%", or "<1%" for a share that would otherwise round down to nothing. */
internal fun Float.percentText(): String {
    val percent = (this * 100).roundToInt()
    return if (percent == 0 && this > 0f) "<1%" else "$percent%"
}
