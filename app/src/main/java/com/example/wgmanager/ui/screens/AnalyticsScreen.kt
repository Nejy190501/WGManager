package com.example.wgmanager.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*
import androidx.compose.ui.platform.LocalContext

private val StatsOrange = Color(0xFFF59E0B)

@Composable
fun AnalyticsScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val s = AppStrings
    val context = LocalContext.current
    val StatsGreen = Color(0xFF10B981)
    val user = DataStore.currentUser ?: return
    var period by remember { mutableStateOf(s.period30d) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Aufgaben, 1 = Finanzen

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back, tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(s.stats, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    // Period pills
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(s.period7d, s.period30d, s.periodYear).forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (period == p) Color(0xFF374151) else Color.Transparent,
                                modifier = Modifier.clickable { period = p }
                            ) {
                                Text(p, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Tab selector
                Surface(shape = RoundedCornerShape(30.dp), color = MaterialTheme.colorScheme.surface) {
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        TabPill(s.tasksTabEmoji, selectedTab == 0, StatsGreen) { selectedTab = 0 }
                        TabPill(s.financesTabEmoji, selectedTab == 1, StatsGreen) { selectedTab = 1 }
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            when (selectedTab) {
                0 -> AufgabenTab()
                1 -> FinanzenTab()
            }

            Spacer(Modifier.height(16.dp))

            // Export button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().clickable { PdfExporter.exportBilanz(context) }
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(s.exportReport, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        DashboardBottomNav(current = AppScreen.ANALYTICS, onNavigate = onNavigate)
    }
}

@Composable
private fun RowScope.TabPill(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = if (selected) color else Color.Transparent,
        modifier = Modifier.weight(1f).clickable(onClick = onClick)
    ) {
        Text(label, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun AufgabenTab() {
    val s = AppStrings
    val StatsGreen = Color(0xFF10B981)
    val completedCount = DataStore.getCompletedTasksCount()
    val topContributor = DataStore.getTopContributor()
    val tasksByPerson = DataStore.getTasksByPerson()
    val maxTasks = tasksByPerson.values.maxOrNull()?.toFloat() ?: 1f

    // ── Entry animation ──
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationStarted = true }

    // Staggered bar animation
    val barAnimations = tasksByPerson.entries.mapIndexed { idx, _ ->
        val anim by animateFloatAsState(
            targetValue = if (animationStarted) 1f else 0f,
            animationSpec = tween(
                durationMillis = 800,
                delayMillis = idx * 150,
                easing = FastOutSlowInEasing
            ),
            label = "bar_$idx"
        )
        anim
    }

    // Count-up animation for ERLEDIGT
    val animatedCount by animateIntAsState(
        targetValue = if (animationStarted) completedCount else 0,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "countUp"
    )

    // Stat cards row
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
        // ERLEDIGT card
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.completed, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text("$animatedCount", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📈", fontSize = 12.sp)
                            Text(" ${s.vsLastMonth}", fontSize = 10.sp, color = StatsGreen)
                        }
                    }
                    Text("✨", fontSize = 32.sp, modifier = Modifier.offset(y = (-4).dp))
                }
            }
        }
        // TOP PERFORMER card
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.topPerformer, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = StatsOrange, modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(topContributor.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(topContributor, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$completedCount ${s.tasksSuffix}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    Text("🏆", fontSize = 28.sp, modifier = Modifier.offset(y = (-4).dp).graphicsLayer(alpha = 0.4f))
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // ── Animated Bar Chart with tap-to-show-value ──
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.distribution, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(20.dp))
            if (tasksByPerson.isEmpty()) {
                Text(s.noData, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(20.dp))
            } else {
                var selectedBarIdx by remember { mutableIntStateOf(-1) }
                val barColors = listOf(Color(0xFF818CF8), Color(0xFF34D399), Color(0xFFF472B6), Color(0xFFFBBF24))
                val entries = tasksByPerson.entries.toList()

                entries.forEachIndexed { idx, (name, count) ->
                    val color = barColors[idx % barColors.size]
                    val fraction = if (maxTasks > 0) count / maxTasks else 0f
                    val animFraction = fraction * barAnimations.getOrElse(idx) { 1f }
                    val isSelected = selectedBarIdx == idx

                    // Animated highlight
                    val bgAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 0.1f else 0f,
                        animationSpec = tween(200),
                        label = "barBg_$idx"
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = bgAlpha),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBarIdx = if (isSelected) -1 else idx }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(name, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, modifier = Modifier.width(50.dp))
                                Spacer(Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f).height(26.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.background)) {
                                    Box(modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = animFraction)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(color, color.copy(alpha = 0.7f))
                                            )
                                        )
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "${count.toInt()}",
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            // Tooltip on selection
                            if (isSelected) {
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = color.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "$name: ${count.toInt()} ${s.tasksSuffix} — ${"%.0f".format(fraction * 100)}%",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = color,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanzenTab() {
    val s = AppStrings
    val StatsGreen = Color(0xFF10B981)
    val boughtItems = DataStore.shoppingItems.filter { it.status == ShoppingStatus.BOUGHT }
    val totalSpent = boughtItems.sumOf { it.price }
    val topBuyer = boughtItems.groupBy { it.boughtBy }.maxByOrNull { it.value.sumOf { item -> item.price } }

    // ── Entry animation ──
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationStarted = true }

    // Count-up for total
    val animatedTotal by animateFloatAsState(
        targetValue = if (animationStarted) totalSpent.toFloat() else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "totalCountUp"
    )

    // Line chart draw progress
    val lineProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(1500, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "lineProgress"
    )

    // Donut sweep animation
    val donutProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(1200, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "donutProgress"
    )

    // Stat cards row
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
        // GESAMTAUSGABEN card
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.totalSpending, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text("${"%.0f".format(animatedTotal)}€", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📈", fontSize = 12.sp)
                            Text(" ${s.vsLastMonth}", fontSize = 10.sp, color = StatsGreen)
                        }
                    }
                    Text("💵", fontSize = 28.sp, modifier = Modifier.offset(y = (-4).dp))
                }
            }
        }
        // TOP PERFORMER card
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.topPerformer, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val buyerName = topBuyer?.key ?: "?"
                            val buyerTotal = topBuyer?.value?.sumOf { it.price } ?: 0.0
                            Surface(shape = CircleShape, color = Color(0xFF06B6D4), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(buyerName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(buyerName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${"%.0f".format(buyerTotal)}€", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    Text("🏆", fontSize = 28.sp, modifier = Modifier.offset(y = (-4).dp).graphicsLayer(alpha = 0.4f))
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // ══════════════════════════════════════════════════
    // ANIMATED LINE CHART — with touch point reveal
    // ══════════════════════════════════════════════════
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📈", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(s.trend, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(16.dp))

            val dataPoints = listOf(12f, 28f, 19f, 42f, 35f, 51f, 45f)
            val labels = if (AppStrings.currentLanguage == AppLanguage.DE) listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So") else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val maxVal = dataPoints.max()
            var selectedPointIdx by remember { mutableIntStateOf(-1) }
            val density = LocalDensity.current
            val surfaceColor = MaterialTheme.colorScheme.surface

            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val stepX = size.width.toFloat() / (dataPoints.size - 1)
                                val nearest = ((offset.x / stepX) + 0.5f).toInt().coerceIn(0, dataPoints.size - 1)
                                selectedPointIdx = if (selectedPointIdx == nearest) -1 else nearest
                            }
                        }
                ) {
                    val chartHeight = size.height - 20.dp.toPx()
                    val stepX = size.width / (dataPoints.size - 1)

                    // Grid lines
                    for (i in 0..3) {
                        val y = chartHeight - (chartHeight * i / 3f)
                        drawLine(surfaceColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    }

                    // Determine how many points to draw based on animation progress
                    val totalPoints = dataPoints.size
                    val pointsToDraw = (totalPoints * lineProgress).toInt().coerceAtMost(totalPoints)
                    val partialFraction = (totalPoints * lineProgress) - pointsToDraw

                    if (pointsToDraw > 0) {
                        // Build the animated path
                        val path = Path()
                        val drawnPoints = mutableListOf<Offset>()

                        for (i in 0 until pointsToDraw.coerceAtMost(totalPoints)) {
                            val x = i * stepX
                            val y = chartHeight - (dataPoints[i] / maxVal * chartHeight)
                            drawnPoints.add(Offset(x, y))
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        // Partial segment
                        if (pointsToDraw < totalPoints && partialFraction > 0) {
                            val prevX = (pointsToDraw - 1) * stepX
                            val prevY = chartHeight - (dataPoints[pointsToDraw - 1] / maxVal * chartHeight)
                            val nextX = pointsToDraw * stepX
                            val nextY = chartHeight - (dataPoints[pointsToDraw] / maxVal * chartHeight)
                            val x = prevX + (nextX - prevX) * partialFraction
                            val y = prevY + (nextY - prevY) * partialFraction
                            path.lineTo(x, y)
                            drawnPoints.add(Offset(x, y))
                        }

                        // Line
                        drawPath(path, color = StatsGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                        // Fill under
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(drawnPoints.last().x, chartHeight)
                            lineTo(0f, chartHeight)
                            close()
                        }
                        drawPath(fillPath, brush = Brush.verticalGradient(listOf(StatsGreen.copy(alpha = 0.3f), Color.Transparent)))

                        // Dots
                        for (i in 0 until pointsToDraw.coerceAtMost(totalPoints)) {
                            val x = i * stepX
                            val y = chartHeight - (dataPoints[i] / maxVal * chartHeight)
                            val isSelected = selectedPointIdx == i
                            if (isSelected) {
                                drawCircle(StatsGreen.copy(alpha = 0.3f), radius = 14.dp.toPx(), center = Offset(x, y))
                            }
                            drawCircle(StatsGreen, radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(), center = Offset(x, y))
                            drawCircle(Color.White, radius = if (isSelected) 3.dp.toPx() else 2.dp.toPx(), center = Offset(x, y))
                        }

                        // Tooltip for selected point
                        if (selectedPointIdx in dataPoints.indices && selectedPointIdx < pointsToDraw) {
                            val px = selectedPointIdx * stepX
                            val py = chartHeight - (dataPoints[selectedPointIdx] / maxVal * chartHeight)
                            // Vertical guide line
                            drawLine(StatsGreen.copy(alpha = 0.4f), Offset(px, 0f), Offset(px, chartHeight), strokeWidth = 1.dp.toPx())
                            // Tooltip bubble
                            val text = "${"%.0f".format(dataPoints[selectedPointIdx])}€"
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = with(density) { 12.sp.toPx() }
                                isAntiAlias = true
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                            val textWidth = paint.measureText(text)
                            val bubbleW = textWidth + 20.dp.toPx()
                            val bubbleH = 28.dp.toPx()
                            val bx = (px - bubbleW / 2).coerceIn(0f, size.width - bubbleW)
                            val by = (py - bubbleH - 12.dp.toPx()).coerceAtLeast(0f)
                            drawRoundRect(Color(0xFF374151), Offset(bx, by), Size(bubbleW, bubbleH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()))
                            drawContext.canvas.nativeCanvas.drawText(text, bx + 10.dp.toPx(), by + 19.dp.toPx(), paint)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEach { Text(it, color = MaterialTheme.colorScheme.outline, fontSize = 11.sp) }
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // ══════════════════════════════════════════════════
    // ANIMATED DONUT CHART — with tap-to-highlight
    // ══════════════════════════════════════════════════
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PieChart, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.categories, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(20.dp))

            val categories = listOf(
                Triple(s.drinks, Color(0xFF22C55E), 0.35f),
                Triple(s.householdCat, Color(0xFF818CF8), 0.40f),
                Triple(s.otherCat, Color(0xFFF59E0B), 0.25f)
            )
            var selectedSegment by remember { mutableIntStateOf(-1) }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier
                        .size(160.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // Determine which segment was tapped based on angle
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                var angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                angle = (angle + 90f + 360f) % 360f // Normalize from top

                                var cumAngle = 0f
                                categories.forEachIndexed { idx, (_, _, pct) ->
                                    val sweep = pct * 360f
                                    if (angle in cumAngle..(cumAngle + sweep)) {
                                        selectedSegment = if (selectedSegment == idx) -1 else idx
                                        return@detectTapGestures
                                    }
                                    cumAngle += sweep
                                }
                            }
                        }
                ) {
                    val ringWidth = 28.dp.toPx()
                    val pad = 14.dp.toPx()
                    var startAngle = -90f
                    categories.forEachIndexed { idx, (_, color, pct) ->
                        val sweep = pct * 360f * donutProgress
                        val isSelected = selectedSegment == idx
                        val strokeW = if (isSelected) ringWidth + 8.dp.toPx() else ringWidth
                        val arcPad = if (isSelected) pad - 4.dp.toPx() else pad
                        drawArc(
                            color = if (isSelected) color else color.copy(alpha = 0.85f),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Butt),
                            topLeft = Offset(arcPad, arcPad),
                            size = Size(size.width - arcPad * 2, size.height - arcPad * 2)
                        )
                        startAngle += sweep
                    }
                }
                // Center text — show selected category or total
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedSegment in categories.indices) {
                        val (name, color, pct) = categories[selectedSegment]
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
                        Text("${"%.0f".format(pct * totalSpent)}€", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${"%.0f".format(pct * 100)}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("${"%.0f".format(animatedTotal)}€", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(s.totalUpper, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            // Legend — interactive
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                categories.forEachIndexed { idx, (label, color, pct) ->
                    val isActive = selectedSegment == idx
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActive) color.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { selectedSegment = if (isActive) -1 else idx }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (isActive) "$label ${"%.0f".format(pct * 100)}%" else label,
                                fontSize = 11.sp,
                                color = color,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}


