package com.example.wgmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

// Pink/Magenta theme colors
private val CalendarPink = Color(0xFFDB2777)
private val CalendarPinkLight = Color(0xFFF472B6)
private val CalendarPinkDark = Color(0xFFBE185D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val s = AppStrings
    val context = LocalContext.current
    val user = DataStore.currentUser ?: return
    val isAdmin = user.role != UserRole.USER
    var refreshKey by remember { mutableIntStateOf(0) }
    var filter by remember { mutableStateOf<EventType?>(null) }
    var showAddEvent by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    val sdf = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY) }
    val todayStr = remember { sdf.format(Date()) }

    val events = remember(refreshKey, filter) {
        DataStore.events.filter { filter == null || it.type == filter }.sortedBy {
            try { sdf.parse(it.date)?.time ?: 0L } catch (_: Exception) { 0L }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Pink gradient header
        Box(
            modifier = Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(CalendarPink, CalendarPinkLight))
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back, tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("📅", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(s.wgCalendar, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    // Clear past
                    IconButton(onClick = {
                        val removed = DataStore.clearPastEvents(todayStr)
                        toast.show(if (removed) s.oldEventsDeleted else s.noOldEvents)
                        refreshKey++
                    }) {
                        Icon(Icons.Default.Delete, "Clear past", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Filter chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterPill(s.allFilter, filter == null, CalendarPink) { filter = null }
                    EventType.entries.forEach { t ->
                        FilterPill(t.name, filter == t, CalendarPink) { filter = if (filter == t) null else t }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // Events list
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (events.isEmpty()) {
                item { EmptyState(emoji = "📅", message = s.noEvents) }
            }

            itemsIndexed(events, key = { _, e -> e.id }) { idx, event ->
                val isToday = event.date == todayStr
                val isPast = try {
                    val eventDate = sdf.parse(event.date)
                    val today = sdf.parse(todayStr)
                    eventDate != null && today != null && eventDate.before(today)
                } catch (_: Exception) { false }

                AnimatedListItem(index = idx) {
                    EventCard(
                        event = event,
                        isToday = isToday,
                        isPast = isPast,
                        onClick = { selectedEvent = event }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        DashboardBottomNav(current = AppScreen.CALENDAR, onNavigate = onNavigate, isAdmin = isAdmin)
    }

    // FAB for adding
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { showAddEvent = true },
            containerColor = CalendarPink,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.padding(end = 24.dp, bottom = 90.dp).size(56.dp)
        ) { Icon(Icons.Default.Add, "Add", modifier = Modifier.size(28.dp)) }
    }

    // Add event dialog
    if (showAddEvent) {
        EventEditDialog(
            title = s.newEventLabel,
            event = null,
            onDismiss = { showAddEvent = false },
            onSave = { title, date, type, emoji ->
                DataStore.addEvent(CalendarEvent(title = title, date = date, type = type, emoji = emoji, createdBy = user.name))
                toast.show(s.eventAdded); showAddEvent = false; refreshKey++
            }
        )
    }

    // Event detail dialog
    selectedEvent?.let { event ->
        EventDetailDialog(
            event = event,
            isAdmin = isAdmin,
            onDismiss = { selectedEvent = null },
            onDelete = {
                DataStore.removeEvent(event); toast.show(s.eventDeleted); selectedEvent = null; refreshKey++
            },
            onEdit = { editingEvent = event; selectedEvent = null },
            onExport = { PdfExporter.exportCalendar(context); selectedEvent = null }
        )
    }

    // Edit event dialog
    editingEvent?.let { event ->
        EventEditDialog(
            title = s.edit,
            event = event,
            onDismiss = { editingEvent = null },
            onSave = { title, date, type, emoji ->
                event.title = title; event.date = date; event.type = type; event.emoji = emoji
                FirebaseSync.pushEvent(event)
                toast.show(s.eventUpdated); editingEvent = null; refreshKey++
            }
        )
    }
}

@Composable
private fun FilterPill(text: String, selected: Boolean, accentColor: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.15f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            color = if (selected) accentColor else Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EventCard(event: CalendarEvent, isToday: Boolean, isPast: Boolean, onClick: () -> Unit) {
    val s = AppStrings
    // Parse date for display
    val dayNum: String
    val monthStr: String
    try {
        val parts = event.date.split(".")
        dayNum = parts[0]
        monthStr = s.monthsShort.getOrElse(parts[1].toInt() - 1) { "" }
    } catch (_: Exception) {
        return
    }

    val borderModifier = if (isToday) Modifier.border(2.dp, CalendarPink, RoundedCornerShape(16.dp)) else Modifier

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().then(borderModifier).clickable(onClick = onClick)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                // Date badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isToday) CalendarPink.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(dayNum, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp,
                            color = if (isToday) CalendarPink else MaterialTheme.colorScheme.onSurface)
                        Text(monthStr, style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) CalendarPink else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }

                Spacer(Modifier.width(14.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(event.emoji, fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            event.title,
                            color = if (isPast) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            textDecoration = if (isPast) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Type chip
                        val chipColor = when (event.type) {
                            EventType.PARTY -> WGWarning
                            EventType.QUIET -> CalendarPink
                            EventType.VISIT -> WGSuccess
                            EventType.GENERAL -> WGInfo
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = chipColor) {
                            Text(event.type.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White, style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Text(s.byLabel, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                        // Creator avatar
                        val creator = DataStore.users.find { it.name == event.createdBy }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(emoji = creator?.avatarEmoji ?: "👤", size = 18)
                            Spacer(Modifier.width(4.dp))
                            Text(event.createdBy, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // "HEUTE" badge
        if (isToday) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CalendarPink,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 8.dp)
            ) {
                Text(s.todayLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color.White, style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun EventDetailDialog(
    event: CalendarEvent,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit
) {
    val s = AppStrings
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Header: Details + Close
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(s.detailsLabel, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
                        Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Large emoji in circle
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(CalendarPink.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) { Text(event.emoji, fontSize = 40.sp) }

                Spacer(Modifier.height(16.dp))

                // Event title
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)

                Spacer(Modifier.height(8.dp))

                // Date badge pill
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏱", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(event.date, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Created by section
                Text(s.createdByLabel2, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                val creator = DataStore.users.find { it.name == event.createdBy }
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(emoji = creator?.avatarEmoji ?: "👤", size = 28)
                        Spacer(Modifier.width(10.dp))
                        Text(event.createdBy, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Export .ics button
                OutlinedButton(
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("⬇ ${s.exportIcs}")
                }

                Spacer(Modifier.height(8.dp))

                // Edit + Delete row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) { Text(s.editBtnIcon, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }
                    if (isAdmin) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                                .background(WGDanger.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = WGDanger)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventEditDialog(
    title: String,
    event: CalendarEvent?,
    onDismiss: () -> Unit,
    onSave: (String, String, EventType, String) -> Unit
) {
    val s = AppStrings
    var eventTitle by remember { mutableStateOf(event?.title ?: "") }
    var date by remember { mutableStateOf(event?.date ?: SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY).format(Date())) }
    var type by remember { mutableStateOf(event?.type ?: EventType.GENERAL) }
    var typeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var recurrence by remember { mutableStateOf(s.weeklyMonthRepeat) }
    var recurrenceExpanded by remember { mutableStateOf(false) }
    val recurrenceOptions = listOf(s.noRepeat, s.weeklyRepeat, s.monthlyRepeat, s.weeklyMonthRepeat)

    val emojiForType = { t: EventType ->
        when (t) { EventType.PARTY -> "🎉"; EventType.QUIET -> "🤫"; EventType.VISIT -> "🏠"; EventType.GENERAL -> "📅" }
    }
    var emoji by remember { mutableStateOf(event?.emoji ?: emojiForType(type)) }

    // DatePicker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY).parse(date)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) { System.currentTimeMillis() }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text(s.ok) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(s.cancel) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title + close
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Emoji + Title
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                    OutlinedTextField(
                        value = eventTitle, onValueChange = { eventTitle = it },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = CalendarPink.copy(alpha = 0.5f), focusedBorderColor = CalendarPink,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = { Text(s.eventPlaceholder, color = MaterialTheme.colorScheme.outline) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Date + Type
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Date field with calendar picker
                    OutlinedTextField(
                        value = date, onValueChange = { date = it },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true,
                        readOnly = false,
                        leadingIcon = { Text("⏱", fontSize = 14.sp) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }, modifier = Modifier.size(24.dp)) {
                                Text("📅", fontSize = 16.sp)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedBorderColor = CalendarPink,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // Type dropdown
                    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = type.name, onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedBorderColor = CalendarPink,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            EventType.entries.forEach { t ->
                                DropdownMenuItem(text = { Text("${emojiForType(t)} ${t.name}") }, onClick = {
                                    type = t; emoji = emojiForType(t); typeExpanded = false
                                })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Recurrence selector
                ExposedDropdownMenuBox(expanded = recurrenceExpanded, onExpandedChange = { recurrenceExpanded = it }) {
                    OutlinedTextField(
                        value = recurrence, onValueChange = {}, readOnly = true,
                        leadingIcon = { Text("🔄", fontSize = 14.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedBorderColor = CalendarPink,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    ExposedDropdownMenu(expanded = recurrenceExpanded, onDismissRequest = { recurrenceExpanded = false }) {
                        recurrenceOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = {
                                recurrence = opt; recurrenceExpanded = false
                            })
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = {
                        if (eventTitle.isNotBlank() && date.isNotBlank()) {
                            // Normalize date from dd/MM/yyyy to dd.MM.yyyy for storage
                            val normalizedDate = date.replace("/", ".")
                            onSave(eventTitle, normalizedDate, type, emoji)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CalendarPink)
                ) { Text(if (event != null) s.save else s.newEventLabel, fontWeight = FontWeight.Bold) }

                // Cancel for edit mode
                if (event != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onDismiss, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CalendarPink)
                        ) { Text(s.cancel) }
                        Button(
                            onClick = {
                                if (eventTitle.isNotBlank() && date.isNotBlank()) {
                                    val normalizedDate = date.replace("/", ".")
                                    onSave(eventTitle, normalizedDate, type, emoji)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CalendarPink)
                        ) { Text(s.save, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
