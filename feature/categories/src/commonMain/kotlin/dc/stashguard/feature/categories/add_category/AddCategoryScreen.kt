@file:OptIn(ExperimentalMaterial3Api::class)

package dc.stashguard.feature.categories.add_category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.ui.categoryIcon
import dc.stashguard.core.ui.categoryIconNames
import org.koin.compose.viewmodel.koinViewModel

/** The theme's indigo, as for new accounts. */
val DefaultCategoryColor = Color(0xFF3F51B5)

/**
 * Two rows of six hues. Category icons are drawn in white on these. Every color of the default
 * categories is here, so editing one of them shows its color as selected.
 */
private val categoryColors = listOf(
    Color(0xFFF44336), // Red
    Color(0xFFE91E63), // Pink
    Color(0xFF9C27B0), // Purple
    DefaultCategoryColor,
    Color(0xFF2196F3), // Blue
    Color(0xFF00BCD4), // Cyan
    Color(0xFF4CAF50), // Green
    Color(0xFFFFC107), // Amber
    Color(0xFFFF9800), // Orange
    Color(0xFF795548), // Brown
    Color(0xFF607D8B), // Blue Grey
    Color(0xFF9E9E9E)  // Grey
)

private const val GRID_COLUMNS = 6

val CategoryType.label: String
    get() = when (this) {
        CategoryType.EXPENSE -> "Expense"
        CategoryType.REVENUE -> "Revenue"
        CategoryType.BOTH -> "Expense & Revenue"
    }

@Composable
fun AddCategoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddCategoryViewModel = koinViewModel()
) {
    val form by viewModel.form.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Add Category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryForm(
                form = form,
                error = error,
                onNameChange = viewModel::updateName,
                onTypeChange = viewModel::updateType,
                onColorChange = viewModel::updateColor,
                onIconChange = viewModel::updateIcon,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { viewModel.saveCategory(onSuccess = onNavigateBack) },
                enabled = !isSaving,
                shape = MaterialTheme.shapes.medium,
                // As tall as the text field above
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Add Category")
            }
        }
    }
}

/**
 * The fields shared by the Add and Edit Category screens, scrolling on their own so the save
 * button below stays in place. [typeLockedReason], when set, disables the type choice and says why.
 */
@Composable
fun CategoryForm(
    form: CategoryFormState,
    error: String?,
    onNameChange: (String) -> Unit,
    onTypeChange: (CategoryType) -> Unit,
    onColorChange: (Color) -> Unit,
    onIconChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    typeLockedReason: String? = null,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CategoryPreview(form = form, modifier = Modifier.fillMaxWidth())

        CategoryNameInput(
            name = form.name,
            onNameChange = onNameChange,
            modifier = Modifier.fillMaxWidth()
        )

        FormSection(title = "Type") {
            CategoryTypeSelector(
                selectedType = form.type,
                onTypeChange = onTypeChange,
                enabled = typeLockedReason == null,
                modifier = Modifier.fillMaxWidth()
            )
            typeLockedReason?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        FormSection(title = "Color") {
            SelectionGrid(items = categoryColors) { color ->
                SelectionCircle(
                    selected = color == form.color,
                    onClick = { onColorChange(color) },
                    background = color
                ) {
                    if (color == form.color) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        FormSection(title = "Icon") {
            SelectionGrid(items = categoryIconNames) { iconName ->
                val selected = iconName == form.iconName
                // The selected icon is drawn as the category will look
                SelectionCircle(
                    selected = selected,
                    onClick = { onIconChange(iconName) },
                    background = if (selected) form.color else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        imageVector = categoryIcon(iconName),
                        contentDescription = iconName,
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CategoryPreview(
    form: CategoryFormState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(form.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon(form.iconName),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = form.name.trim().ifEmpty { "New category" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = form.type.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryNameInput(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Category name") },
        placeholder = { Text("e.g. Groceries, Rent") },
        leadingIcon = { Icon(Icons.Outlined.Category, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun CategoryTypeSelector(
    selectedType: CategoryType,
    onTypeChange: (CategoryType) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    // Expense first, as on the categories list
    val types = listOf(CategoryType.EXPENSE, CategoryType.REVENUE, CategoryType.BOTH)
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                selected = type == selectedType,
                onClick = { onTypeChange(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                enabled = enabled
            ) {
                Text(
                    when (type) {
                        CategoryType.BOTH -> "Both"
                        else -> type.label
                    }
                )
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}

/** A fixed-column grid laid out with rows, so it can sit inside the form's vertical scroll. */
@Composable
private fun <T> SelectionGrid(
    items: List<T>,
    item: @Composable (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(GRID_COLUMNS).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        item(it)
                    }
                }
                // Keep the last row's cells the same width as the others
                repeat(GRID_COLUMNS - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SelectionCircle(
    selected: Boolean,
    onClick: () -> Unit,
    background: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .widthIn(max = 56.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(background)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
