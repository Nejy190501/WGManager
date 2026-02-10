package com.example.wgmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext

private val ShopGreen = Color(0xFF10B981)
private val ShopGreenLight = Color(0xFF34D399)

@Composable
fun ShoppingScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val user = DataStore.currentUser ?: return
    val s = AppStrings
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Liste, 1 = Bilanz, 2 = Vorrat
    var refreshKey by remember { mutableIntStateOf(0) }
    var newItemName by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }
    var showSettleDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingItem?>(null) }
    val isAdmin = user.role == UserRole.ADMIN || user.role == UserRole.SUPER_ADMIN

    val items = remember(refreshKey) { DataStore.shoppingItems.toList() }
    val pending = items.filter { it.status == ShoppingStatus.PENDING }
    val bought = items.filter { it.status == ShoppingStatus.BOUGHT }
    val totalSpent = bought.sumOf { it.price }
    val monthlyBudget = DataStore.getMonthlyBudget()
    var selectedEmoji by remember { mutableStateOf("📦") }
    val emojiChoices = listOf("📦", "🥛", "🧻", "🍝", "🍺", "🍞", "🥚", "🧴", "🥫", "🍫", "🧹", "🍎", "🧀", "🥩", "☕", "🍪")
    var showEmojiPicker by remember { mutableStateOf(false) }

    val quickChips = listOf(s.quickChipBeer, s.quickChipCleaning, s.quickChipSnacks)
    val itemEmojis = mapOf(
        "Oat Milk" to "🥛", "Toilet Paper" to "🧻", "Pasta" to "🍝",
        "Tomato Sauce" to "🥫", "Beer" to "🍺", "Bread" to "🍞",
        "Eggs" to "🥚", "Milk" to "🥛", "Dish Soap" to "🧴"
    )

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Green gradient header
        Box(modifier = Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(ShopGreen, ShopGreenLight))
        )) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back, tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(s.shoppingList, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { PdfExporter.exportShoppingList(context) }) {
                        Icon(Icons.Default.PictureAsPdf, "Export PDF", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Tab pills
                Surface(shape = RoundedCornerShape(30.dp), color = Color.White.copy(alpha = 0.15f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        ShopTabPill(s.list, selectedTab == 0) { selectedTab = 0 }
                        ShopTabPill(s.balance, selectedTab == 1) { selectedTab = 1 }
                        ShopTabPill(s.vorrat, selectedTab == 2) { selectedTab = 2 }
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Liste tab
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Budget bar
                    item {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${s.monthlyBudget} (${monthlyBudget.toInt()}€)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    Spacer(Modifier.weight(1f))
                                    if (isAdmin) {
                                        IconButton(onClick = { showBudgetDialog = true }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Edit, "Edit Budget", tint = ShopGreen, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text("${"%.0f".format(totalSpent)}€ / ${monthlyBudget.toInt()}€", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { (totalSpent / monthlyBudget).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = ShopGreen,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Pending total
                    item {
                        val pendingTotal = pending.sumOf { it.price }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(s.totalOpenLabel, color = MaterialTheme.colorScheme.outline, fontSize = 13.sp)
                            Spacer(Modifier.weight(1f))
                            Text("${"%.2f".format(pendingTotal)}€", color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Quick add chips
                    item {
                        Text("⏱️ ${s.quickAdd}", color = MaterialTheme.colorScheme.outline, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            quickChips.forEach { chip ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable {
                                        newItemName = chip.drop(2).trim()
                                    }
                                ) {
                                    Text(chip, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // All items (pending + bought)
                    val allItems = (pending + bought).sortedBy { it.status == ShoppingStatus.BOUGHT }
                    itemsIndexed(allItems, key = { _, item -> item.id }) { _, item ->
                        val isBought = item.status == ShoppingStatus.BOUGHT
                        val emoji = item.emoji

                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox
                                Surface(
                                    shape = CircleShape,
                                    color = if (isBought) ShopGreen else Color.Transparent,
                                    border = if (!isBought) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
                                    modifier = Modifier.size(32.dp).clickable {
                                        if (!isBought) {
                                            DataStore.buyItem(item)
                                            FirebaseSync.pushShoppingItem(item.copy(status = ShoppingStatus.BOUGHT, boughtBy = user.name))
                                            refreshKey++
                                        }
                                    }
                                ) {
                                    if (isBought) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                Spacer(Modifier.width(10.dp))

                                // Emoji
                                Text(emoji, fontSize = 22.sp)
                                Spacer(Modifier.width(10.dp))

                                // Item info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.name,
                                        color = if (isBought) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        textDecoration = if (isBought) TextDecoration.LineThrough else null
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val avatarEmoji = DataStore.getUserAvatarEmoji(item.addedBy)
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(avatarEmoji, fontSize = 10.sp)
                                            }
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            item.addedBy,
                                            color = MaterialTheme.colorScheme.outline,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Price
                                Text(
                                    "${"%.2f".format(item.price)}€",
                                    color = ShopGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(10.dp))

                                // Delete button
                                if (isAdmin) {
                                    IconButton(
                                        onClick = { editingItem = item },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                }
                                IconButton(
                                    onClick = {
                                        DataStore.removeShoppingItem(item)
                                        FirebaseSync.removeShoppingItem(item.id)
                                        refreshKey++
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }

                // Bottom add bar
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        // Emoji picker row
                        if (showEmojiPicker) {
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(AppStrings.chooseEmoji, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        emojiChoices.forEach { e ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (selectedEmoji == e) ShopGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.size(40.dp).clickable {
                                                    selectedEmoji = e
                                                    showEmojiPicker = false
                                                }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(e, fontSize = 20.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Emoji selector button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                                modifier = Modifier.size(52.dp).clickable { showEmojiPicker = !showEmojiPicker }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(selectedEmoji, fontSize = 24.sp)
                                }
                            }
                            Spacer(Modifier.width(8.dp))

                            // Item name input
                            OutlinedTextField(
                                value = newItemName, onValueChange = { newItemName = it },
                                modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                                placeholder = { Text(s.addItem, color = MaterialTheme.colorScheme.outline, fontSize = 13.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant, focusedBorderColor = ShopGreen,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = ShopGreen
                                )
                            )
                            Spacer(Modifier.width(8.dp))

                            // Price input
                            OutlinedTextField(
                                value = newItemPrice, onValueChange = { newItemPrice = it },
                                modifier = Modifier.width(70.dp).height(52.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                                placeholder = { Text("0.00", color = MaterialTheme.colorScheme.outline, fontSize = 13.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant, focusedBorderColor = ShopGreen,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = ShopGreen
                                )
                            )
                            Spacer(Modifier.width(8.dp))

                            // Add button
                            FloatingActionButton(
                                onClick = {
                                    if (newItemName.isNotBlank()) {
                                        val price = newItemPrice.toDoubleOrNull() ?: 0.0
                                        val item = ShoppingItem(
                                            id = "s${System.currentTimeMillis()}",
                                            name = newItemName,
                                            price = price,
                                            addedBy = user.name,
                                            status = ShoppingStatus.PENDING,
                                            boughtBy = "",
                                            emoji = selectedEmoji
                                        )
                                        DataStore.shoppingItems.add(item)
                                        FirebaseSync.pushShoppingItem(item)
                                        newItemName = ""
                                        newItemPrice = ""
                                        selectedEmoji = "📦"
                                        showEmojiPicker = false
                                        refreshKey++
                                        toast.show(s.itemAdded)
                                    }
                                },
                                containerColor = ShopGreen,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(Icons.Default.Add, "Add", modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
            1 -> {
                // Bilanz tab - Expense distribution matching screenshot
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Scrollable content
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header card with wallet icon
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text("💳", fontSize = 28.sp, modifier = Modifier.padding(12.dp))
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(s.expenseDistribution, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(s.basedOnActivity, color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                                }
                                // PDF export button
                                IconButton(
                                    onClick = { PdfExporter.exportBilanz(context) },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, s.exportPdf, tint = ShopGreen, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // Calculate fair share and balances
                        val allUsers = DataStore.users
                        val totalSpentAll = bought.sumOf { it.price }
                        val fairShare = if (allUsers.isNotEmpty()) totalSpentAll / allUsers.size else 0.0
                        
                        // User balance cards
                        allUsers.forEach { member ->
                            val paidByMember = bought.filter { it.boughtBy == member.name }.sumOf { it.price }
                            val balance = paidByMember - fairShare
                            val isPositive = balance >= 0
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Colored left border
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(72.dp)
                                            .background(
                                                if (isPositive) ShopGreen else Color(0xFFEF4444),
                                                RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                                            )
                                    )
                                    
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                member.name,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "${s.paid}: ${"%.2f".format(paidByMember)}€",
                                                color = MaterialTheme.colorScheme.outline,
                                                fontSize = 12.sp
                                            )
                                        }
                                        
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "${if (isPositive) "+" else ""}${"%.2f".format(balance)}€",
                                                color = if (isPositive) ShopGreen else Color(0xFFEF4444),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                if (isPositive) s.receives else s.owes,
                                                color = if (isPositive) ShopGreen else Color(0xFFEF4444),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (allUsers.isEmpty()) {
                            Text(s.noMembersYet, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
                        }
                    }

                    // Settle debts button - pinned at bottom (admin only)
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Button(
                            onClick = {
                                if (isAdmin) {
                                    showSettleDialog = true
                                } else {
                                    toast.show(s.adminOnly)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopGreen)
                        ) {
                            Text("🤝", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(s.settleDebts, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                DashboardBottomNav(current = AppScreen.SHOPPING, onNavigate = onNavigate)
            }
            2 -> {
                // Vorrat (Pantry) tab
                val pantryList = remember(refreshKey) { DataStore.pantryItems.toList() }
                var showAddPantry by remember { mutableStateOf(false) }
                var newPantryName by remember { mutableStateOf("") }
                var newPantryEmoji by remember { mutableStateOf("📦") }
                val pantryEmojis = listOf("📦", "🥛", "🍚", "🍝", "☕", "🧈", "🍞", "🥫", "🫒", "🧴", "🥚", "🧀", "🍎", "🥩", "🫘", "🍫")

                Column(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("📦 ${s.pantrySubtitle}", color = MaterialTheme.colorScheme.outline, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(Modifier.height(8.dp))
                        }

                        if (pantryList.isEmpty()) {
                            item {
                                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📦", fontSize = 40.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Text(s.noPantryItems, color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        itemsIndexed(pantryList, key = { _, item -> item.id }) { _, item ->
                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.emoji, fontSize = 24.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val avatarEmoji = DataStore.getUserAvatarEmoji(item.updatedBy)
                                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(16.dp)) {
                                                Box(contentAlignment = Alignment.Center) { Text(avatarEmoji, fontSize = 9.sp) }
                                            }
                                            Spacer(Modifier.width(4.dp))
                                            Text(item.updatedBy, color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                                        }
                                    }

                                    // Status buttons - everyone can change
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val statuses = listOf(
                                            Triple(PantryStatus.FULL, s.pantryFull, Color(0xFF22C55E)),
                                            Triple(PantryStatus.LOW, s.pantryLow, Color(0xFFF59E0B)),
                                            Triple(PantryStatus.EMPTY, s.pantryEmpty, Color(0xFFEF4444))
                                        )
                                        statuses.forEach { (status, label, color) ->
                                            val isActive = item.status == status
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isActive) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                                border = if (isActive) ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                                    width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(color)
                                                ) else null,
                                                modifier = Modifier.clickable {
                                                    DataStore.updatePantryStatus(item, status)
                                                    refreshKey++
                                                    toast.show(s.pantryStatusUpdated)
                                                }
                                            ) {
                                                Text(
                                                    label,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    color = if (isActive) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            DataStore.removePantryItem(item)
                                            refreshKey++
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }

                    // Add pantry item section
                    if (showAddPantry) {
                        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(s.addPantryItem, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    pantryEmojis.forEach { em ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (newPantryEmoji == em) ShopGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(38.dp).clickable { newPantryEmoji = em }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) { Text(em, fontSize = 18.sp) }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = newPantryName, onValueChange = { newPantryName = it },
                                        modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                                        placeholder = { Text(s.pantryItemName, color = MaterialTheme.colorScheme.outline, fontSize = 13.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant, focusedBorderColor = ShopGreen,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.background, focusedContainerColor = MaterialTheme.colorScheme.background,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = ShopGreen
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    FloatingActionButton(
                                        onClick = {
                                            if (newPantryName.isNotBlank()) {
                                                DataStore.addPantryItem(newPantryName, newPantryEmoji)
                                                newPantryName = ""
                                                newPantryEmoji = "📦"
                                                showAddPantry = false
                                                refreshKey++
                                                toast.show(s.pantryAdded)
                                            }
                                        },
                                        containerColor = ShopGreen, contentColor = Color.White,
                                        shape = RoundedCornerShape(14.dp), modifier = Modifier.size(52.dp)
                                    ) {
                                        Icon(Icons.Default.Add, "Add", modifier = Modifier.size(28.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Button(
                                    onClick = { showAddPantry = true },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ShopGreen)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(s.addPantryItem, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                DashboardBottomNav(current = AppScreen.SHOPPING, onNavigate = onNavigate)
            }
        }

        if (selectedTab == 0) {
            DashboardBottomNav(current = AppScreen.SHOPPING, onNavigate = onNavigate)
        }
    }

    // ── BUDGET EDIT DIALOG ──────────────────────────────────
    if (showBudgetDialog) {
        var budgetInput by remember { mutableStateOf(monthlyBudget.toInt().toString()) }
        Dialog(onDismissRequest = { showBudgetDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(s.setBudget, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = budgetInput, onValueChange = { budgetInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(s.budgetLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopGreen, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = ShopGreen, unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showBudgetDialog = false }) {
                            Text(s.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val b = budgetInput.toDoubleOrNull()
                                if (b != null && b > 0) {
                                    DataStore.updateWGBudget(b)
                                    toast.show(s.budgetUpdated)
                                    showBudgetDialog = false
                                    refreshKey++
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShopGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(s.save, color = Color.White) }
                    }
                }
            }
        }
    }

    // ── ITEM EDIT DIALOG ──────────────────────────────────
    editingItem?.let { item ->
        var editName by remember { mutableStateOf(item.name) }
        var editPrice by remember { mutableStateOf("%.2f".format(item.price)) }
        var editEmoji by remember { mutableStateOf(item.emoji) }
        Dialog(onDismissRequest = { editingItem = null }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(s.editItem, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        emojiChoices.forEach { e ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (editEmoji == e) ShopGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(38.dp).clickable { editEmoji = e }
                            ) {
                                Box(contentAlignment = Alignment.Center) { Text(e, fontSize = 18.sp) }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editName, onValueChange = { editName = it },
                        label = { Text(s.addItem) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopGreen, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = ShopGreen, unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPrice, onValueChange = { editPrice = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("€") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShopGreen, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = ShopGreen, unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingItem = null }) {
                            Text(s.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editName.isNotBlank()) {
                                    item.name = editName
                                    item.price = editPrice.toDoubleOrNull() ?: item.price
                                    item.emoji = editEmoji
                                    FirebaseSync.pushShoppingItem(item)
                                    toast.show(s.itemUpdated)
                                    editingItem = null
                                    refreshKey++
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShopGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(s.save, color = Color.White) }
                    }
                }
            }
        }
    }

    // ── SETTLE DEBTS DIALOG ──────────────────────────────────
    if (showSettleDialog) {
        val allUsers = DataStore.users
        val boughtItems = DataStore.shoppingItems.filter { it.status == ShoppingStatus.BOUGHT }
        val totalSpentAll = boughtItems.sumOf { it.price }
        val fairShare = if (allUsers.isNotEmpty()) totalSpentAll / allUsers.size else 0.0

        // Creditors: people who overpaid (positive balance)
        val creditors = allUsers.mapNotNull { member ->
            val paidByMember = boughtItems.filter { it.boughtBy == member.name }.sumOf { it.price }
            val balance = paidByMember - fairShare
            if (balance > 0.01) member to balance else null
        }

        // Confirmation state
        var confirmingCreditor by remember { mutableStateOf<Pair<User, Double>?>(null) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showSettleDialog = false }) {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    if (confirmingCreditor != null) {
                        // ── Confirmation step ──
                        val (creditor, amount) = confirmingCreditor!!
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💸", fontSize = 42.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                s.settleConfirmTitle,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                s.settleConfirmMsg
                                    .replace("%s", creditor.name, false)
                                    .replaceFirst("%s", "${"%.2f".format(amount)}€"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(Modifier.height(24.dp))

                            // Confirm button
                            Button(
                                onClick = {
                                    DataStore.settleDebtWith(creditor.name)
                                    refreshKey++
                                    toast.show("${s.settleDebtsPaid} ${creditor.name} — ${"%.2f".format(amount)}€ ✅")
                                    confirmingCreditor = null
                                    showSettleDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ShopGreen)
                            ) {
                                Text("✅", fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(s.settleDebts, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Spacer(Modifier.height(8.dp))

                            // Back
                            TextButton(onClick = { confirmingCreditor = null }) {
                                Text(s.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            }
                        }

                    } else {
                        // ── Main view: creditor list ──
                        Text(
                            s.settleDebts,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            s.settleDebtsWho,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(20.dp))

                        if (creditors.isEmpty()) {
                            // No debts
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("✅", fontSize = 36.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    s.noDebts,
                                    color = ShopGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            // Creditor cards matching screenshot
                            creditors.forEach { (creditor, amount) ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { confirmingCreditor = creditor to amount }
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Name on the left
                                        Text(
                                            creditor.name,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        // Green settle text on the right
                                        Text(
                                            "${s.settleDebts} ${"%.2f".format(amount)}€",
                                            color = ShopGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Cancel
                        TextButton(
                            onClick = { showSettleDialog = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                s.cancel,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ShopTabPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = if (selected) Color.White else Color.Transparent,
        modifier = Modifier.weight(1f).clickable(onClick = onClick)
    ) {
        Text(label, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
