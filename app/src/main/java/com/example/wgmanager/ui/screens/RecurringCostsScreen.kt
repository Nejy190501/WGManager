package com.example.wgmanager.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringCostsScreen(
    onNavigate: (AppScreen) -> Unit,
    toast: ToastState
) {
    val palette = LocalThemePalette.current
    val str = AppStrings
    val context = LocalContext.current
    val members = DataStore.getWGMembers()
    val costs = DataStore.recurringCosts
    val isAdmin = DataStore.currentUser?.role == UserRole.ADMIN || DataStore.currentUser?.role == UserRole.SUPER_ADMIN

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCost by remember { mutableStateOf<RecurringCost?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }

    // Force recomposition
    key(refreshKey) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("💸 ${str.recurringCosts}", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        IconButton(onClick = { PdfExporter.exportCostReport(context) }) {
                            Icon(Icons.Default.PictureAsPdf, "Export PDF", tint = palette.accent)
                        }
                        if (isAdmin) {
                            IconButton(onClick = { showAddDialog = true }) {
                                Icon(Icons.Default.Add, "Add", tint = palette.accent)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Summary card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(listOf(palette.accent, palette.primaryDark)),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(
                                    str.recurringCostsSubtitle,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(str.totalMonthly, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                        Text(
                                            "€%.2f".format(DataStore.getRecurringCostTotal()),
                                            color = Color.White,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(str.perPerson, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                        Text(
                                            "€%.2f".format(DataStore.getRecurringCostPerPerson()),
                                            color = Color.White,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (members.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "${members.size} ${str.membersList.lowercase()}",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Section header
                item {
                    Text(
                        str.activeCosts,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (costs.isEmpty()) {
                    item {
                        EmptyState(
                            emoji = "💸",
                            message = str.noCosts
                        )
                    }
                } else {
                    itemsIndexed(costs) { index, cost ->
                        AnimatedListItem(index = index) {
                            CostCard(
                                cost = cost,
                                memberCount = members.size,
                                isAdmin = isAdmin,
                                onToggle = {
                                    DataStore.toggleRecurringCost(cost)
                                    refreshKey++
                                },
                                onRemove = {
                                    DataStore.removeRecurringCost(cost)
                                    toast.show(str.costRemoved)
                                    refreshKey++
                                },
                                onEdit = {
                                    editingCost = cost
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        AddCostDialog(
            members = members,
            onDismiss = { showAddDialog = false },
            onAdd = { name, emoji, amount, paidBy ->
                DataStore.addRecurringCost(name, emoji, amount, paidBy)
                toast.show(str.costAdded)
                showAddDialog = false
                refreshKey++
            }
        )
    }

    // Edit dialog
    editingCost?.let { cost ->
        AddCostDialog(
            members = members,
            existingCost = cost,
            onDismiss = { editingCost = null },
            onAdd = { name, emoji, amount, paidBy ->
                cost.name = name; cost.emoji = emoji; cost.totalAmount = amount; cost.paidBy = paidBy
                DataStore.updateRecurringCost(cost)
                toast.show(str.costUpdated)
                editingCost = null
                refreshKey++
            }
        )
    }
}

@Composable
private fun CostCard(
    cost: RecurringCost,
    memberCount: Int,
    isAdmin: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val palette = LocalThemePalette.current
    val bgColor by animateColorAsState(
        if (cost.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        label = "costBg"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(palette.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(cost.emoji, fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cost.name,
                    color = if (cost.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    "${AppStrings.paidBy}: ${cost.paidBy}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                if (memberCount > 0) {
                    Text(
                        "€%.2f / ${AppStrings.perPerson.lowercase()}".format(cost.totalAmount / memberCount),
                        color = palette.accent.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "€%.2f".format(cost.totalAmount),
                    color = if (cost.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    if (cost.frequency == RecurringFrequency.MONTHLY) AppStrings.monthly else AppStrings.weekly,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            if (isAdmin) {
                Spacer(Modifier.width(8.dp))
                Column {
                    IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (cost.isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            "Toggle",
                            tint = if (cost.isActive) WGSuccess else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Remove", tint = WGDanger.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCostDialog(
    members: List<User>,
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, String) -> Unit,
    existingCost: RecurringCost? = null
) {
    val palette = LocalThemePalette.current
    val str = AppStrings
    var name by remember { mutableStateOf(existingCost?.name ?: "") }
    var emoji by remember { mutableStateOf(existingCost?.emoji ?: "💸") }
    var amount by remember { mutableStateOf(existingCost?.totalAmount?.let { "%.2f".format(it) } ?: "") }
    var paidBy by remember { mutableStateOf(existingCost?.paidBy ?: DataStore.currentUser?.name ?: "") }

    val emojiOptions = listOf("💸", "📶", "🎬", "⚡", "📺", "🏠", "🚿", "🔥", "📱", "🎵")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (existingCost != null) str.editCost else str.addCost, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))

                // Emoji picker row
                Text(str.costEmoji, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    emojiOptions.forEach { e ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (emoji == e) palette.accent.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(e, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(str.costName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.accent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = palette.accent,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(str.costAmount) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.accent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = palette.accent,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(12.dp))
                Text(str.paidBy, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    members.forEach { member ->
                        FilterChip(
                            selected = paidBy == member.name,
                            onClick = { paidBy = member.name },
                            label = { Text(member.name, fontSize = 12.sp) },
                            leadingIcon = { Text(member.avatarEmoji, fontSize = 14.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.accent.copy(alpha = 0.3f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(str.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val a = amount.toDoubleOrNull()
                            if (name.isNotBlank() && a != null && a > 0) {
                                onAdd(name, emoji, a, paidBy)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(str.save, color = Color.White)
                    }
                }
            }
        }
    }
}
