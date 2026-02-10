package com.example.wgmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*
import androidx.compose.ui.text.style.TextAlign

// Orange theme colors
private val MealOrange = Color(0xFFEA580C)
private val MealOrangeLight = Color(0xFFF97316)
private val MealOrangeBg = Color(0xFFFB923C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val s = AppStrings
    val user = DataStore.currentUser ?: return
    val isAdmin = user.role != UserRole.USER
    var refreshKey by remember { mutableIntStateOf(0) }
    var showRecipePicker by remember { mutableStateOf<MealPlanDay?>(null) }
    var showAddRecipe by remember { mutableStateOf(false) }
    var showDayDetail by remember { mutableStateOf<MealPlanDay?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Wochenplan, 1 = Gerichte
    var editingRecipe by remember { mutableStateOf<Recipe?>(null) }

    val plan = remember(refreshKey) { DataStore.mealPlan.toList() }
    val plannedCount = plan.count { it.recipeId != null }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Orange Header
        Box(
            modifier = Modifier.fillMaxWidth().background(
                Brush.verticalGradient(listOf(MealOrange, MealOrangeBg))
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back, tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("🍽️", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(s.mealPlannerTitle, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                    if (isAdmin) {
                        IconButton(onClick = {
                            // Auto assign cooks
                            plan.filter { it.cook.isBlank() && it.recipeId != null }.forEach { d ->
                                d.cook = DataStore.getWGMembers().randomOrNull()?.name ?: ""
                                DataStore.saveMealPlanDay(d)
                            }
                            toast.show(s.cooksAssigned); refreshKey++
                        }) {
                            Icon(Icons.Default.Shuffle, s.autoAssign, tint = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                // Tab pills
                Surface(shape = RoundedCornerShape(30.dp), color = Color.White.copy(alpha = 0.15f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = if (selectedTab == 0) Color.White else Color.Transparent,
                            modifier = Modifier.weight(1f).clickable { selectedTab = 0 }
                        ) {
                            Text(s.weekPlanTab, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center,
                                color = if (selectedTab == 0) MealOrange else Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = if (selectedTab == 1) Color.White else Color.Transparent,
                            modifier = Modifier.weight(1f).clickable { selectedTab = 1 }
                        ) {
                            Text(s.dishesTab, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center,
                                color = if (selectedTab == 1) MealOrange else Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        when (selectedTab) {
            0 -> {
        // Week plan
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(plan, key = { _, d -> d.day }) { idx, day ->
                val recipe = DataStore.getRecipeById(day.recipeId)
                AnimatedListItem(index = idx) {
                    if (recipe != null) {
                        // Planned day card
                        PlannedDayCard(day = day, recipe = recipe, onCartClick = {
                            DataStore.addIngredientsToShopping(recipe)
                            toast.show(s.ingredientsAddedShopping)
                        }, onClick = { showDayDetail = day })
                    } else {
                        // Empty day card
                        EmptyDayCard(day = day, onClick = { showRecipePicker = day })
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
            } // end tab 0
            1 -> {
                // Gerichte (Dishes) tab — independent section to add/edit/delete dishes
                val recipes = remember(refreshKey) { DataStore.recipes.toList() }
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    // Add new dish card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MealOrange,
                            modifier = Modifier.fillMaxWidth().clickable { showAddRecipe = true }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(s.createRecipe, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    if (recipes.isEmpty()) {
                        item { EmptyState(emoji = "🍽️", message = s.noDishesYet) }
                    }

                    itemsIndexed(recipes, key = { _, r -> r.id }) { idx, recipe ->
                        AnimatedListItem(index = idx) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(recipe.emoji, fontSize = 28.sp)
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(recipe.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("⏱ ${recipe.timeMinutes} ${s.minUnit}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                            Text("🔥 ${recipe.difficulty}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                        }
                                        if (recipe.ingredients.isNotEmpty()) {
                                            Text(recipe.ingredients.joinToString(", "), color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    // Edit button
                                    IconButton(onClick = { editingRecipe = recipe }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Edit, s.edit, tint = MealOrange, modifier = Modifier.size(20.dp))
                                    }
                                    // Delete button
                                    IconButton(onClick = {
                                        DataStore.removeRecipe(recipe)
                                        toast.show(s.dishDeleted)
                                        refreshKey++
                                    }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Delete, s.delete, tint = WGDanger, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            } // end tab 1
        } // end when

        DashboardBottomNav(current = AppScreen.MEAL_PLANNER, onNavigate = onNavigate)
    }

    // Recipe picker bottom sheet style dialog
    showRecipePicker?.let { day ->
        RecipePickerDialog(
            day = day,
            onDismiss = { showRecipePicker = null },
            onSelectRecipe = { recipe ->
                day.recipeId = recipe.id
                DataStore.saveMealPlanDay(day)
                showRecipePicker = null; refreshKey++
            },
            onCreateNew = { showAddRecipe = true; showRecipePicker = null },
            toast = toast
        )
    }

    // Day detail dialog
    showDayDetail?.let { day ->
        val recipe = DataStore.getRecipeById(day.recipeId)
        if (recipe != null) {
            Dialog(onDismissRequest = { showDayDetail = null }) {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${day.day} — ${recipe.emoji}", fontSize = 20.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { showDayDetail = null }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, s.close, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        Text("${s.cookLabelMeal} ${day.cook.ifEmpty { s.notAssigned }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("⏱ ${recipe.timeMinutes} ${s.minUnit} • ${recipe.difficulty}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(s.ingredientsLabel, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        recipe.ingredients.forEach { Text("• $it", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    DataStore.addIngredientsToShopping(recipe)
                                    toast.show(s.ingredientsAddedShopping)
                                    showDayDetail = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MealOrange)
                            ) { Text(s.addToShoppingBtn) }
                            if (isAdmin) {
                                OutlinedButton(
                                    onClick = {
                                        day.recipeId = null; day.cook = ""
                                        DataStore.saveMealPlanDay(day)
                                        toast.show(s.dayCleared); showDayDetail = null; refreshKey++
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WGDanger)
                                ) { Text(s.removeLabel) }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add recipe dialog
    if (showAddRecipe) {
        AddRecipeDialog(
            onDismiss = { showAddRecipe = false },
            onAdd = { name, emoji, difficulty, time, ingredients ->
                DataStore.addRecipe(Recipe(name = name, emoji = emoji, difficulty = difficulty, timeMinutes = time, ingredients = ingredients))
                toast.show(s.recipeCreated); showAddRecipe = false; refreshKey++
            }
        )
    }

    // Edit recipe dialog
    editingRecipe?.let { recipe ->
        AddRecipeDialog(
            existingRecipe = recipe,
            onDismiss = { editingRecipe = null },
            onAdd = { name, emoji, difficulty, time, ingredients ->
                recipe.name = name; recipe.emoji = emoji; recipe.difficulty = difficulty
                recipe.timeMinutes = time; recipe.ingredients = ingredients
                toast.show(s.dishUpdated); editingRecipe = null; refreshKey++
            }
        )
    }
}

@Composable
private fun PlannedDayCard(day: MealPlanDay, recipe: Recipe, onCartClick: () -> Unit, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Day badge (orange)
            Box(
                modifier = Modifier.width(56.dp).fillMaxHeight()
                    .background(MealOrange, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(day.day.uppercase().take(3), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }

            // Content
            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Text(recipe.emoji, fontSize = 22.sp)
                Text(recipe.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱ ${recipe.timeMinutes} ${AppStrings.minUnit}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text("🔥 ${recipe.difficulty}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    if (day.cook.isNotBlank()) {
                        Text("👤 ${day.cook}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Cart button
            IconButton(onClick = onCartClick, modifier = Modifier.padding(end = 8.dp)) {
                Icon(Icons.Default.ShoppingCart, "To cart", tint = WGSuccess, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun EmptyDayCard(day: MealPlanDay, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Day badge (gray)
            Box(
                modifier = Modifier.width(56.dp).padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(day.day.uppercase().take(3), color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Plus + Planen
            Row(
                modifier = Modifier.weight(1f).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, AppStrings.planBtn, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                }
                Text(AppStrings.planAction, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun RecipePickerDialog(
    day: MealPlanDay,
    onDismiss: () -> Unit,
    onSelectRecipe: (Recipe) -> Unit,
    onCreateNew: () -> Unit,
    toast: ToastState
) {
    val recipes = DataStore.recipes.toList()
    val ls = AppStrings
    var filterMode by remember { mutableStateOf(ls.filterAll) }
    val filters = listOf(ls.filterAll, ls.filterQuick, ls.filterEasy, ls.filterElaborate)

    val filtered = recipes.filter { recipe ->
        when (filterMode) {
            ls.filterQuick -> recipe.timeMinutes < 30
            ls.filterEasy -> recipe.difficulty == "Easy"
            ls.filterElaborate -> recipe.difficulty == "Hard" || recipe.difficulty == "Medium"
            else -> true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp).heightIn(max = 500.dp)) {
                // Handle bar
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
                }
                Spacer(Modifier.height(16.dp))

                // Title + close
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ls.chooseDish, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, ls.close, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Filter chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEach { f ->
                        val selected = filterMode == f
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) MealOrange else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable { filterMode = f }
                        ) {
                            Text(
                                f, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Recipe grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // New recipe card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                .clickable(onClick = onCreateNew)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(4.dp))
                                Text(ls.newRecipe, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Existing recipes
                    items(filtered) { recipe ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                .clickable { onSelectRecipe(recipe) }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(14.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(recipe.emoji, fontSize = 28.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(recipe.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp))
                                Text("⏱ ${recipe.timeMinutes} • ${recipe.difficulty}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRecipeDialog(onDismiss: () -> Unit, existingRecipe: Recipe? = null, onAdd: (String, String, String, Int, List<String>) -> Unit) {
    var name by remember { mutableStateOf(existingRecipe?.name ?: "") }
    var emoji by remember { mutableStateOf(existingRecipe?.emoji ?: "🍳") }
    val rs = AppStrings
    var difficulty by remember { mutableStateOf(
        when (existingRecipe?.difficulty) {
            "Easy" -> rs.difficultyEasy; "Hard" -> rs.difficultyHard; else -> rs.difficultyMedium
        }
    ) }
    var time by remember { mutableStateOf(existingRecipe?.timeMinutes?.toString() ?: "30") }
    var ingredients by remember { mutableStateOf(existingRecipe?.ingredients?.joinToString(", ") ?: "") }
    var diffExpanded by remember { mutableStateOf(false) }

    val emojiOptions = listOf("🍳", "🍕", "🍔", "🍝", "🥗", "🍛", "🌮", "🥘")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(rs.createRecipe, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(20.dp))

                // Emoji + Name
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Emoji picker circle
                    var showEmojiPicker by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(48.dp).clickable { showEmojiPicker = !showEmojiPicker }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(emoji, fontSize = 24.sp)
                            }
                        }
                        DropdownMenu(expanded = showEmojiPicker, onDismissRequest = { showEmojiPicker = false }) {
                            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                emojiOptions.forEach { e ->
                                    Text(e, fontSize = 24.sp, modifier = Modifier.clickable { emoji = e; showEmojiPicker = false })
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant, focusedBorderColor = MealOrange,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = { Text(rs.dishName, color = MaterialTheme.colorScheme.outline) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Time + Difficulty
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⏱", fontSize = 16.sp)
                        Spacer(Modifier.width(4.dp))
                        OutlinedTextField(
                            value = time, onValueChange = { time = it },
                            modifier = Modifier.width(60.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant, focusedBorderColor = MealOrange,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(rs.minUnit, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }

                    ExposedDropdownMenuBox(expanded = diffExpanded, onExpandedChange = { diffExpanded = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = difficulty, onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant, focusedBorderColor = MealOrange,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        ExposedDropdownMenu(expanded = diffExpanded, onDismissRequest = { diffExpanded = false }) {
                            listOf(rs.difficultyEasy to "Easy", rs.difficultyMedium to "Medium", rs.difficultyHard to "Hard").forEach { (label, val_) ->
                                DropdownMenuItem(text = { Text(label) }, onClick = { difficulty = label; diffExpanded = false })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Ingredients
                OutlinedTextField(
                    value = ingredients, onValueChange = { ingredients = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant, focusedBorderColor = MealOrange,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    placeholder = { Text(rs.ingredientsComma, color = MaterialTheme.colorScheme.outline) }
                )

                Spacer(Modifier.height(20.dp))

                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(rs.cancel, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val difficultyEn = when (difficulty) { rs.difficultyEasy -> "Easy"; rs.difficultyHard -> "Hard"; else -> "Medium" }
                                onAdd(name, emoji, difficultyEn, time.toIntOrNull() ?: 30,
                                    ingredients.split(",").map { it.trim() }.filter { it.isNotBlank() })
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MealOrange)
                    ) { Text(rs.save, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
