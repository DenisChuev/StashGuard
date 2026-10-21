@file:OptIn(ExperimentalMaterial3Api::class)

package dc.stashguard.feature.operations.add_operation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.core.common.CURRENCY_SYMBOL
import dc.stashguard.core.common.DateUtils
import dc.stashguard.core.common.formatReadable
import dc.stashguard.core.common.toCurrencyString
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.ui.BalanceVisualTransformation
import dc.stashguard.core.ui.color
import dc.stashguard.core.ui.icon
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddOperationScreen(
    accountId: String,
    operationType: OperationType,
    onNavigateBack: () -> Unit,
    viewModel: AddOperationViewModel = koinViewModel {
        parametersOf(accountId, operationType)
    }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val availableAccounts by viewModel.availableAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    Scaffold(
        topBar = {
            OperationTopAppBar(
                operationType = operationType,
                onNavigateBack = onNavigateBack
            )
        },
        content = { paddingValues ->
            AddOperationContent(
                state = state,
                operationType = operationType,
                currentAccountId = accountId,
                availableAccounts = availableAccounts,
                availableCategories = availableCategories,
                onAmountChange = viewModel::updateAmount,
                onCategoryChange = viewModel::updateCategory,
                onToAccountChange = viewModel::updateToAccount,
                onDateChange = viewModel::updateDate,
                onNoteChange = viewModel::updateNote,
                onSave = { viewModel.saveOperation(onNavigateBack) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}

@Composable
fun CategorySelectionSection(
    selectedCategoryId: String,
    availableCategories: List<Category>,
    onCategoryChange: (String) -> Unit,
    operationType: OperationType,
    modifier: Modifier = Modifier
) {
    val filteredCategories = availableCategories.filter { category ->
        when (operationType) {
            OperationType.REVENUE ->
                category.type == CategoryType.REVENUE || category.type == CategoryType.BOTH

            OperationType.EXPENSE ->
                category.type == CategoryType.EXPENSE || category.type == CategoryType.BOTH

            OperationType.TRANSFER ->
                category.type == CategoryType.BOTH // Only show "both" categories for transfers
        }
    }

    val selectedCategory = availableCategories.find { it.id == selectedCategoryId }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Category Preview
            if (selectedCategory != null) {
                SelectedCategoryPreview(
                    category = selectedCategory,
                    onClear = { onCategoryChange("") }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Category Grid
            if (filteredCategories.isEmpty()) {
                Text(
                    text = "No categories available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onSelect = { onCategoryChange(category.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Flat inside the section's card, so selection shows by color rather than stacked shadows
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                category.color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        onClick = onSelect
    ) {
        // Full width so the icon and name are centered in the grid cell
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(category.color, CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    category.icon,
                    category.name,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SelectedCategoryPreview(
    category: Category,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = category.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(category.color, CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear selection"
                )
            }
        }
    }
}

@Composable
fun FixedTransferCategorySection(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF9E9E9E), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Transfer",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Transfer",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Money movement between accounts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OperationTopAppBar(
    operationType: OperationType,
    onNavigateBack: () -> Unit,
    isEditing: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val (title, icon, color) = when (operationType) {
        OperationType.REVENUE -> Triple(
            if (isEditing) "Edit Revenue" else "Add Revenue",
            Icons.AutoMirrored.Filled.TrendingUp,
            Color(0xFF4CAF50)
        )

        OperationType.EXPENSE -> Triple(
            if (isEditing) "Edit Expense" else "Add Expense",
            Icons.AutoMirrored.Filled.TrendingDown,
            Color(0xFFF44336)
        )

        OperationType.TRANSFER -> Triple(
            if (isEditing) "Edit Transfer" else "Transfer Money",
            Icons.Default.SwapHoriz,
            Color(0xFF2196F3)
        )
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title)
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        },
        actions = actions
    )
}

@Composable
fun AddOperationContent(
    state: OperationState,
    operationType: OperationType,
    currentAccountId: String,
    availableAccounts: List<Account>,
    availableCategories: List<Category>,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onToAccountChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    // Editing the receiving side of a transfer: the picked account is the sender
    isIncomingTransfer: Boolean = false,
    isEditing: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // The form scrolls while the save button stays at the bottom, as in Add Account
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Amount Input
            AmountInputSection(
                amount = state.amount,
                onAmountChange = onAmountChange,
                operationType = operationType
            )

            // Category Selection (for all operation types except transfer)
            if (operationType != OperationType.TRANSFER) {
                CategorySelectionSection(
                    selectedCategoryId = state.categoryId,
                    availableCategories = availableCategories,
                    onCategoryChange = onCategoryChange,
                    operationType = operationType
                )
            } else {
                // For transfers, use a fixed "Transfer" category
                FixedTransferCategorySection()
            }

            // To Account Selection (for Transfer)
            if (operationType == OperationType.TRANSFER) {
                TransferAccountSection(
                    title = if (isIncomingTransfer) "Transfer From" else "Transfer To",
                    toAccountId = state.toAccountId,
                    availableAccounts = availableAccounts,
                    currentAccountId = currentAccountId,
                    onToAccountChange = onToAccountChange
                )
            }

            // Date Selection
            DateSelectionSection(
                date = state.date,
                onDateChange = onDateChange
            )

            // Note Input
            NoteInputSection(
                note = state.note,
                onNoteChange = onNoteChange
            )

            // Error Message
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Save Button
        SaveOperationButton(
            isLoading = state.isLoading,
            operationType = operationType,
            isEditing = isEditing,
            onSave = onSave,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AmountInputSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    operationType: OperationType,
    modifier: Modifier = Modifier
) {
    val label = when (operationType) {
        OperationType.REVENUE -> "Revenue Amount"
        OperationType.EXPENSE -> "Expense Amount"
        OperationType.TRANSFER -> "Transfer Amount"
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    // Allow only numbers and a decimal point with up to 2 decimals
                    if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        onAmountChange(newValue)
                    }
                },
                label = { Text("Enter amount") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Text(
                        text = CURRENCY_SYMBOL,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                visualTransformation = BalanceVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TransferAccountSection(
    title: String,
    toAccountId: String,
    availableAccounts: List<Account>,
    currentAccountId: String,
    onToAccountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredAccounts = availableAccounts.filter { it.id != currentAccountId }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredAccounts.isEmpty()) {
                Text(
                    text = "No other accounts available for transfer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                filteredAccounts.forEach { account ->
                    AccountSelectionItem(
                        account = account,
                        isSelected = toAccountId == account.id,
                        onSelect = { onToAccountChange(account.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccountSelectionItem(
    account: Account,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        // Flat inside the section's card, so selection shows by color rather than stacked shadows
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(account.color, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = account.balance.toCurrencyString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** How far back the day strip reaches; an operation's own older date is always included. */
private const val DATE_STRIP_DAYS_BACK = 365

private val weekdayFormat = LocalDate.Format { dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED) }
private val monthFormat = LocalDate.Format { monthName(MonthNames.ENGLISH_ABBREVIATED) }

/**
 * A strip of days swiped left (back in time) and right (towards today), ending at today.
 * The selected day, today by default, is scrolled into view. The calendar button before the
 * strip picks any past date, and the strip then reaches back to it.
 */
@Composable
fun DateSelectionSection(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { DateUtils.currentDate() }
    val firstDay = minOf(today.minus(DATE_STRIP_DAYS_BACK, DateTimeUnit.DAY), date)
    val dayCount = (today.toEpochDays() - firstDay.toEpochDays()).toInt() + 1
    val selectedIndex = (date.toEpochDays() - firstDay.toEpochDays()).toInt()
    // A couple of earlier days stay visible to the left of the selected one
    val leadIndex = (selectedIndex - 2).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = leadIndex)
    var showDatePicker by remember { mutableStateOf(false) }

    // Follow the selection when it isn't visible: Edit Operation loads its date after the first
    // frame, and a date from the calendar can be anywhere in the strip
    LaunchedEffect(date) {
        val visible = listState.layoutInfo.visibleItemsInfo.map { it.index }
        if (selectedIndex !in visible) listState.scrollToItem(leadIndex)
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (date == today) "Today" else date.formatReadable(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // The gap sits outside the strip, so days cut off at its edge stay clear of the button
                FilledTonalIconButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.padding(start = 16.dp, end = 12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Pick a date")
                }
                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(count = dayCount) { index ->
                        val day = firstDay.plus(index, DateTimeUnit.DAY)
                        DayItem(
                            day = day,
                            isSelected = day == date,
                            isToday = day == today,
                            onSelect = { onDateChange(day) }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        OperationDatePickerDialog(
            date = date,
            today = today,
            onDateChange = onDateChange,
            onDismiss = { showDatePicker = false }
        )
    }
}

private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L

/** The Material date picker works in UTC milliseconds at the start of the day. */
@Composable
private fun OperationDatePickerDialog(
    date: LocalDate,
    today: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val todayMillis = today.toEpochDays() * MILLIS_PER_DAY
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.toEpochDays() * MILLIS_PER_DAY,
        // Same range as the strip: nothing after today
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
            override fun isSelectableYear(year: Int) = year <= today.year
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onDateChange(LocalDate.fromEpochDays(it / MILLIS_PER_DAY))
                    }
                    onDismiss()
                },
                enabled = pickerState.selectedDateMillis != null
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

@Composable
private fun DayItem(
    day: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    // Flat inside the section's card, like the category and account choices
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = contentColor
        ),
        // Wide enough that about four and a half days show, so the cut-off one hints at swiping
        modifier = modifier.width(76.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Text(text = day.format(weekdayFormat), style = MaterialTheme.typography.labelMedium)
            Row {
                Text(
                    text = day.day.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = day.format(monthFormat),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}

@Composable
fun NoteInputSection(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Note (Optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("Add a note...") },
                placeholder = { Text("Enter any additional information") },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.Notes, "Note")
                },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SaveOperationButton(
    isLoading: Boolean,
    operationType: OperationType,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false
) {
    val buttonText = when {
        isEditing -> "Save Changes"
        operationType == OperationType.REVENUE -> "Add Revenue"
        operationType == OperationType.EXPENSE -> "Add Expense"
        else -> "Complete Transfer"
    }

    Button(
        onClick = onSave,
        enabled = !isLoading,
        shape = MaterialTheme.shapes.medium,
        // As tall as the text fields above, like Add Account's button
        modifier = modifier.height(56.dp)
    ) {
        Text(buttonText)
    }
}
