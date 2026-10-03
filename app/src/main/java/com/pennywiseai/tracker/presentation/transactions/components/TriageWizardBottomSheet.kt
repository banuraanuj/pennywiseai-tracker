package com.pennywiseai.tracker.presentation.transactions.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pennywiseai.tracker.R
import com.pennywiseai.tracker.data.database.entity.TransactionType
import com.pennywiseai.tracker.ui.theme.Dimensions
import com.pennywiseai.tracker.ui.theme.Spacing

enum class TriageStep {
    SELECT_TYPE,
    SELECT_CATEGORY,
    CONFIRM_RULE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageWizardBottomSheet(
    onDismissRequest: () -> Unit,
    merchantName: String,
    currentType: TransactionType?,
    availableCategories: List<String>,
    suggestedCategories: List<String> = emptyList(), // Can be populated by LLM
    onApplyFix: (type: TransactionType, category: String) -> Unit,
    onCreateRule: (merchant: String, type: TransactionType, category: String) -> Unit
) {
    var currentStep by remember { mutableStateOf(TriageStep.SELECT_TYPE) }
    var selectedType by remember { mutableStateOf(currentType ?: TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("") }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .padding(bottom = Spacing.xl)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith 
                    slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                },
                label = "triage_step_transition"
            ) { step ->
                when (step) {
                    TriageStep.SELECT_TYPE -> {
                        TypeSelectionStep(
                            onTypeSelected = { type ->
                                selectedType = type
                                currentStep = TriageStep.SELECT_CATEGORY
                            }
                        )
                    }
                    TriageStep.SELECT_CATEGORY -> {
                        CategorySelectionStep(
                            transactionType = selectedType,
                            categories = availableCategories,
                            suggestedCategories = suggestedCategories,
                            onBack = { currentStep = TriageStep.SELECT_TYPE },
                            onCategorySelected = { category ->
                                selectedCategory = category
                                currentStep = TriageStep.CONFIRM_RULE
                            }
                        )
                    }
                    TriageStep.CONFIRM_RULE -> {
                        ConfirmRuleStep(
                            merchantName = merchantName,
                            type = selectedType,
                            category = selectedCategory,
                            onBack = { currentStep = TriageStep.SELECT_CATEGORY },
                            onApplyOnly = {
                                onApplyFix(selectedType, selectedCategory)
                                onDismissRequest()
                            },
                            onCreateRule = {
                                onCreateRule(merchantName, selectedType, selectedCategory)
                                onApplyFix(selectedType, selectedCategory)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeSelectionStep(
    onTypeSelected: (TransactionType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What kind of transaction is this?",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = Spacing.md)
        )
        
        // Large visual cards for types
        val types = listOf(
            Triple(TransactionType.EXPENSE, Icons.AutoMirrored.Filled.TrendingDown, "Expense"),
            Triple(TransactionType.INCOME, Icons.AutoMirrored.Filled.TrendingUp, "Income"),
            Triple(TransactionType.CREDIT, Icons.Default.CreditCard, "Credit Card"),
            Triple(TransactionType.INVESTMENT, Icons.AutoMirrored.Filled.ShowChart, "Investment"),
            Triple(TransactionType.TRANSFER, Icons.Default.SwapHoriz, "Transfer")
        )
        
        types.forEach { (type, icon, label) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs)
                    .clickable { onTypeSelected(type) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimensions.Icon.medium)
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelectionStep(
    transactionType: TransactionType,
    categories: List<String>,
    suggestedCategories: List<String>,
    onBack: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Which category?",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.sm))

        if (suggestedCategories.isNotEmpty()) {
            Text(
                text = "Suggested",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Spacing.xs)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                suggestedCategories.forEach { category ->
                    FilterChip(
                        selected = false,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
        }

        Text(
            text = "All Categories",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Spacing.xs)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.heightIn(max = 300.dp) // Constrain height in bottom sheet
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.clickable { onCategorySelected(category) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(Spacing.sm).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmRuleStep(
    merchantName: String,
    type: TransactionType,
    category: String,
    onBack: () -> Unit,
    onApplyOnly: () -> Unit,
    onCreateRule: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Learn for next time?",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    "You've classified this as:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${type.name} → $category",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        Text(
            "Should PennyWise always categorize transactions from '$merchantName' this way in the future?",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        Button(
            onClick = onCreateRule,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("Yes, create a Smart Rule")
        }
        
        Spacer(modifier = Modifier.height(Spacing.sm))
        
        OutlinedButton(
            onClick = onApplyOnly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("No, just fix this one")
        }
    }
}