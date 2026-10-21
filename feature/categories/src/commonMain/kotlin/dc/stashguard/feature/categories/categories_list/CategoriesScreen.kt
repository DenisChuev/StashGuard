@file:OptIn(ExperimentalMaterial3Api::class)

package dc.stashguard.feature.categories.categories_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.isTransfer
import dc.stashguard.core.ui.color
import dc.stashguard.core.ui.icon
import dc.stashguard.feature.categories.add_category.label
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (categoryId: String) -> Unit,
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CategoriesTopAppBar(onNavigateBack = onNavigateBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCategory,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Category")
            }
        }
    ) { paddingValues ->
        val loadedCategories = categories
        when {
            loadedCategories == null -> LoadingCategoriesState(modifier = Modifier.padding(paddingValues))
            loadedCategories.isEmpty() -> EmptyCategoriesState(modifier = Modifier.padding(paddingValues))
            else -> CategoriesContent(
                categories = loadedCategories,
                onCategoryClick = { onNavigateToEditCategory(it.id) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun CategoriesTopAppBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                "Categories",
                style = MaterialTheme.typography.headlineSmall
            )
        },
//        navigationIcon = {
//            IconButton(onClick = onNavigateBack) {
//                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
//            }
//        },
        modifier = modifier
    )
}

@Composable
fun CategoriesContent(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    val expenseCategories = categories.filter {
        it.type == CategoryType.EXPENSE || it.type == CategoryType.BOTH
    }
    val revenueCategories = categories.filter {
        it.type == CategoryType.REVENUE || it.type == CategoryType.BOTH
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Expense Categories Section
        if (expenseCategories.isNotEmpty()) {
            item {
                CategorySectionHeader(
                    title = "Expense Categories",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = Color(0xFFF44336)
                )
            }
            items(expenseCategories, key = { "expense_${it.id}" }) { category ->
                CategoryListItem(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }

        // Revenue Categories Section
        if (revenueCategories.isNotEmpty()) {
            item {
                CategorySectionHeader(
                    title = "Revenue Categories",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            items(revenueCategories, key = { "revenue_${it.id}" }) { category ->
                CategoryListItem(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
fun CategorySectionHeader(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CategoryListItem(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Transfer is built in and can't be edited, so its card isn't clickable
    val isEditable = !category.isTransfer
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        enabled = isEditable,
        // Keep the read-only Transfer card looking like the others instead of greyed out
        colors = CardDefaults.cardColors(
            disabledContainerColor = CardDefaults.cardColors().containerColor,
            disabledContentColor = CardDefaults.cardColors().contentColor
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Icon and Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(category.color, CircleShape)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (category.isTransfer) {
                            "Built in, used by transfers"
                        } else {
                            category.type.label
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isEditable) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Category",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingCategoriesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading categories...")
        }
    }
}

@Composable
fun EmptyCategoriesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = "No Categories",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "No categories found",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Add your first category to organize transactions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}