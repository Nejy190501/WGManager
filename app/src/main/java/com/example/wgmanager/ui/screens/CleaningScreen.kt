package com.example.wgmanager.ui.screens

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*
import java.util.Calendar

private val CleanOrange = Color(0xFFF97316)
private val CleanOrangeLight = Color(0xFFFB923C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleaningScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val s = AppStrings
    val user = DataStore.currentUser ?: return
    val isAdmin = user.role != UserRole.USER
    var refreshKey by remember { mutableIntStateOf(0) }
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedAssignee by remember { mutableStateOf<String?>(null) }
    var assigneeExpanded by remember { mutableStateOf(false) }
    val members = remember { DataStore.getWGMembers() }
    val tasks = remember(refreshKey) { DataStore.tasks.toList() }

    // Calculate current week
    val calendar = Calendar.getInstance()
    val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
    val month = s.monthsClean[calendar.get(Calendar.MONTH)]
    val startDay = calendar.get(Calendar.DAY_OF_MONTH)
    val endDay = startDay + 6

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Orange gradient header
        Box(modifier = Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(CleanOrange, CleanOrangeLight))
        )) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back, tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(s.cleaningPlan, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { DataStore.rotateTasks(); toast.show(s.tasksRotated); refreshKey++ }) {
                        Icon(Icons.Default.Refresh, "Rotate", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("${s.weekNum} $weekOfYear", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("$month $startDay - $month $endDay", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        // Task list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(tasks, key = { _, t -> t.id }) { idx, task ->
                val assignee = members.find { it.name == task.assignedTo }
                val initials = task.assignedTo.take(2).uppercase()

                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Avatar with initials
                        Surface(shape = CircleShape, color = Color(0xFF374151), modifier = Modifier.size(42.dp)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))

                        // Task info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                task.title, 
                                color = if (task.completed) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface, 
                                fontWeight = FontWeight.Medium, 
                                fontSize = 15.sp,
                                textDecoration = if (task.completed) TextDecoration.LineThrough else null
                            )
                            Text(task.assignedTo.uppercase(), color = CleanOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            
                            // Action buttons only for uncompleted tasks
                            if (!task.completed && task.assignedTo != user.name) {
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Anstupsen button
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF374151),
                                        modifier = Modifier.clickable { toast.show("${task.assignedTo} ${s.memberNudged}") }
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.NotificationsActive, null, tint = CleanOrange, modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(s.nudge, color = CleanOrange, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    // Strike button
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF374151),
                                        modifier = Modifier.clickable { toast.show(s.strikeGiven) }
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(s.strike, color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    // Delete button
                                    if (isAdmin) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF374151),
                                            modifier = Modifier.clickable(onClick = {
                                                DataStore.removeTask(task)
                                                FirebaseSync.removeTask(task.id)
                                                refreshKey++
                                            })
                                        ) {
                                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(5.dp).size(14.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Checkbox
                        Surface(
                            shape = CircleShape,
                            color = if (task.completed) Color(0xFF22C55E) else Color.Transparent,
                            border = if (!task.completed) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
                            modifier = Modifier.size(36.dp).clickable {
                                DataStore.toggleTask(task)
                                if (task.completed) toast.show(s.xpEarned)
                                else toast.show(s.taskReset)
                                refreshKey++
                            }
                        ) {
                            if (task.completed) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        // Bottom add bar
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Task input
                OutlinedTextField(
                    value = newTaskTitle, onValueChange = { newTaskTitle = it },
                    modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                    placeholder = { Text(s.newTaskPlaceholder, color = MaterialTheme.colorScheme.outline, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant, focusedBorderColor = CleanOrange,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = CleanOrange
                    )
                )
                Spacer(Modifier.width(8.dp))

                // Assignee dropdown
                ExposedDropdownMenuBox(expanded = assigneeExpanded, onExpandedChange = { assigneeExpanded = it }) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.menuAnchor().clickable { assigneeExpanded = true }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedAssignee ?: s.whoPlaceholder, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                        }
                    }
                    ExposedDropdownMenu(expanded = assigneeExpanded, onDismissRequest = { assigneeExpanded = false }) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = { selectedAssignee = member.name; assigneeExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))

                // Add button
                FloatingActionButton(
                    onClick = {
                        if (newTaskTitle.isNotBlank() && selectedAssignee != null) {
                            val task = Task(
                                id = "t${System.currentTimeMillis()}",
                                title = newTaskTitle,
                                assignedTo = selectedAssignee!!,
                                completed = false,
                                streak = 0
                            )
                            DataStore.addTask(task)
                            newTaskTitle = ""
                            selectedAssignee = null
                            refreshKey++
                            toast.show(s.taskCreated)
                        }
                    },
                    containerColor = CleanOrange,
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
