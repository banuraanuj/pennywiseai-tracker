package com.pennywiseai.tracker.presentation.settings.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.pennywiseai.tracker.R
import com.pennywiseai.tracker.data.database.entity.TransactionType
import com.pennywiseai.tracker.ui.components.CustomTitleTopAppBar
import com.pennywiseai.tracker.ui.theme.Dimensions
import com.pennywiseai.tracker.ui.theme.Spacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTemplateScreen(
    templateId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: AddTemplateViewModel = hiltViewModel()
) {
    val senderPattern by viewModel.senderPattern.collectAsState()
    val messageTemplate by viewModel.messageTemplate.collectAsState()
    val transactionType by viewModel.transactionType.collectAsState()
    val testSms by viewModel.testSms.collectAsState()
    val testSender by viewModel.testSender.collectAsState()
    val parsedPreview by viewModel.parsedPreview.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val hazeState = remember { HazeState() }
    var showTypeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(templateId) {
        viewModel.loadTemplate(templateId)
    }

    Scaffold(
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorSmall = scrollBehavior,
                scrollBehaviorLarge = scrollBehavior,
                title = if (templateId != null) "Edit Template" else "Add Template",
                hasBackButton = true,
                navigationContent = {
                    Box(
                        modifier = Modifier
                            .clickable(onClick = onNavigateBack)
                            .padding(horizontal = Spacing.md)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actionContent = {
                    if (templateId != null) {
                        IconButton(onClick = { viewModel.deleteTemplate(onNavigateBack) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Template", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                hazeState = hazeState
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveTemplate(onNavigateBack) },
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = { Text("Save") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(Dimensions.Padding.content),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            OutlinedTextField(
                value = senderPattern,
                onValueChange = viewModel::updateSenderPattern,
                label = { Text("Sender Name (Optional)") },
                placeholder = { Text("e.g. HDFCBK or leave blank for any") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = it }
            ) {
                OutlinedTextField(
                    value = transactionType?.name ?: "Auto Detect",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Transaction Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Auto Detect") },
                        onClick = { viewModel.updateTransactionType(null); showTypeDropdown = false }
                    )
                    TransactionType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = { viewModel.updateTransactionType(type); showTypeDropdown = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = messageTemplate,
                onValueChange = viewModel::updateMessageTemplate,
                label = { Text("Message Template") },
                placeholder = { Text("e.g. Paid {AMOUNT} at {MERCHANT}") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Text(
                text = "Use tags: {AMOUNT}, {MERCHANT}, {ACCOUNT}, {REFERENCE}, {BALANCE}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

            Text("Test Sandbox", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = testSender,
                onValueChange = viewModel::updateTestSender,
                label = { Text("Test Sender") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = testSms,
                onValueChange = viewModel::updateTestSms,
                label = { Text("Paste raw SMS here") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            if (testSms.isNotBlank()) {
                if (parsedPreview != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text("✅ Match Found!", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text("Amount: ${parsedPreview!!.amount}")
                            Text("Type: ${parsedPreview!!.type.name}")
                            if (parsedPreview!!.merchant != null) Text("Merchant: ${parsedPreview!!.merchant}")
                            if (parsedPreview!!.accountLast4 != null) Text("Account: ${parsedPreview!!.accountLast4}")
                            if (parsedPreview!!.reference != null) Text("Reference: ${parsedPreview!!.reference}")
                            if (parsedPreview!!.balance != null) Text("Balance: ${parsedPreview!!.balance}")
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "❌ No match",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(Spacing.md)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // FAB padding
        }
    }
}