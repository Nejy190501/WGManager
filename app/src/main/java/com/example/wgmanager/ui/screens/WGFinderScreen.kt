package com.example.wgmanager.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wgmanager.data.AppStrings
import com.example.wgmanager.data.DataStore
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════
// COLOR PALETTE
// ═══════════════════════════════════════════════════════════════
private val WGBlue = Color(0xFF3B82F6)
private val WGBlueBright = Color(0xFF2563EB)
private val WGPurple = Color(0xFF7C3AED)
private val WGGreen = Color(0xFF22C55E)

// ═══════════════════════════════════════════════════════════════
// DATA MODEL
// ═══════════════════════════════════════════════════════════════
private data class WGTag(val emoji: String, val label: String)
private data class WGMember(val initials: String, val color: Color, val name: String = "")
private data class WGAmenity(val icon: ImageVector, val label: String)

private data class WGPreview(
    val id: String,
    val name: String,
    val distanceKm: Float,
    val tags: List<WGTag>,
    val members: List<WGMember>,
    val capacity: Int,
    val pricePerRoom: Int,
    val aboutText: String,
    val amenities: List<WGAmenity>,
    val joinCode: String = "",
    val wgId: String = "" // real DataStore WG id
)

// ═══════════════════════════════════════════════════════════════
// BUILD WG PREVIEWS FROM REAL DATA
// ═══════════════════════════════════════════════════════════════
private val memberColors = listOf(
    Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFFF59E0B),
    Color(0xFF22C55E), Color(0xFF7C3AED), Color(0xFFEC4899)
)

private fun buildWGPreviews(): List<WGPreview> {
    val realWGs = DataStore.wgs.map { wg ->
        val wgMembers = DataStore.users.filter { it.wgId == wg.id && it.hasWG }
        val members = wgMembers.mapIndexed { i, user ->
            val initials = user.name.take(2).uppercase()
            WGMember(initials, memberColors[i % memberColors.size], user.name)
        }
        WGPreview(
            id = wg.id,
            name = wg.name,
            distanceKm = (1..50).random() / 10f,
            tags = listOf(WGTag("🏠", "WG"), WGTag("👥", "${wgMembers.size} Pers.")),
            members = members,
            capacity = maxOf(wgMembers.size + 1, 4),
            pricePerRoom = if (wg.rentPrice > 0) wg.rentPrice else 400,
            aboutText = wg.publicDescription.ifBlank { "A great place to live! Join us." },
            amenities = wg.amenities.mapNotNull { key ->
                when (key) {
                    "wifi" -> WGAmenity(Icons.Default.Wifi, "WiFi")
                    "washer" -> WGAmenity(Icons.Default.LocalLaundryService, AppStrings.amenityWasher)
                    "dryer" -> WGAmenity(Icons.Default.LocalLaundryService, AppStrings.amenityDryer)
                    "parking" -> WGAmenity(Icons.Default.LocalParking, AppStrings.amenityParking)
                    "balcony" -> WGAmenity(Icons.Default.Balcony, AppStrings.amenityBalcony)
                    "garden" -> WGAmenity(Icons.Default.Park, AppStrings.amenityGarden)
                    "dishwasher" -> WGAmenity(Icons.Default.DinnerDining, AppStrings.amenityDishwasher)
                    "elevator" -> WGAmenity(Icons.Default.Elevator, AppStrings.amenityElevator)
                    "bike_storage" -> WGAmenity(Icons.Default.PedalBike, AppStrings.amenityBikeStorage)
                    "cellar" -> WGAmenity(Icons.Default.Inventory2, AppStrings.amenityCellar)
                    "bathtub" -> WGAmenity(Icons.Default.Bathtub, AppStrings.amenityBathtub)
                    "tv" -> WGAmenity(Icons.Default.Tv, AppStrings.amenityTv)
                    else -> null
                }
            },
            joinCode = wg.joinCode,
            wgId = wg.id
        )
    }
    return realWGs
}

// ═══════════════════════════════════════════════════════════════
// MAIN SCREEN
// ═══════════════════════════════════════════════════════════════
@Composable
fun WGFinderScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val s = AppStrings
    var joinCode by remember { mutableStateOf("") }
    var showDetailFor by remember { mutableStateOf<WGPreview?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var filterParty by remember { mutableStateOf(true) }
    var filterQuiet by remember { mutableStateOf(true) }

    val userName = DataStore.currentUser?.name?.split(" ")?.firstOrNull() ?: ""

    // Build WG list from real DataStore data
    val allWGs = remember { buildWGPreviews() }

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "wgFloat")
    val floatProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "wgFloatAnim"
    )

    // Active filter count
    val activeFilters = listOf(filterParty, filterQuiet).count { it }

    // Show all WGs (no filter logic since we use real data)
    val filteredWGs = allWGs

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── Header Row ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            DataStore.logout()
                            onNavigate(AppScreen.LOGIN)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Online badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(WGGreen)
                        )
                        Text(
                            "14 ${s.searchingOnline}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ─── Search Icon with badge ─────────────────────────
            Box(contentAlignment = Alignment.Center) {
                val logoFloat = sin((floatProgress * 2 * Math.PI).toFloat()) * 6f
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .size(90.dp)
                        .offset(y = logoFloat.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = WGBlue,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
                // Small purple user badge
                Surface(
                    shape = CircleShape,
                    color = WGPurple,
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = logoFloat.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Groups, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Titles ─────────────────────────────────────────
            Text(
                s.wgJoinTitle,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${s.hello} $userName! ${s.wgJoinSubtitle}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(28.dp))

            // ─── Code Join Card ─────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        s.haveACode,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = { joinCode = it.uppercase() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    s.codePlaceholder,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedBorderColor = WGBlue,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                cursorColor = WGBlue
                            ),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )
                        Button(
                            onClick = {
                                if (joinCode.isBlank()) {
                                    toast.show(s.codeEmptyMsg, ToastType.ERROR)
                                } else if (DataStore.joinWGByCode(joinCode.trim())) {
                                    toast.show("${s.wgJoinTitle} ✅ 🎉")
                                    onNavigate(AppScreen.ONBOARDING)
                                } else {
                                    toast.show(s.invalidCodeMsg, ToastType.ERROR)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WGBlueBright),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Text(
                                s.joinBtn,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ─── Suggestions Header ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    s.suggestionsNearby,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    "${s.filter} ($activeFilters)",
                    color = WGBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { showFilterSheet = true }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ─── WG Cards ───────────────────────────────────────
            filteredWGs.forEachIndexed { idx, wg ->
                WGRichCard(
                    wg = wg,
                    s = s,
                    onClick = { showDetailFor = wg }
                )
                if (idx < filteredWGs.lastIndex) Spacer(Modifier.height(14.dp))
            }

            if (filteredWGs.isEmpty()) {
                Spacer(Modifier.height(40.dp))
                Text("🔍", fontSize = 36.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    s.loading,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    // ─── Detail Modal ───────────────────────────────────────
    showDetailFor?.let { wg ->
        WGDetailModal(
            wg = wg,
            s = s,
            onDismiss = { showDetailFor = null },
            onSendRequest = { message ->
                if (wg.wgId.isNotBlank()) {
                    val sent = DataStore.sendJoinRequest(wg.wgId, message)
                    if (sent) {
                        toast.show("${s.requestSentSuccess} ✅")
                    } else {
                        toast.show("${s.alreadyRequested} ⚠️", ToastType.ERROR)
                    }
                } else if (wg.joinCode.isNotBlank()) {
                    DataStore.joinWGByCode(wg.joinCode)
                    toast.show("${s.wgJoinTitle} ✅ 🎉")
                    onNavigate(AppScreen.ONBOARDING)
                }
                showDetailFor = null
            }
        )
    }

    // ─── Filter Dialog ──────────────────────────────────────
    if (showFilterSheet) {
        FilterDialog(
            filterParty = filterParty,
            filterQuiet = filterQuiet,
            onFilterPartyChange = { filterParty = it },
            onFilterQuietChange = { filterQuiet = it },
            onDismiss = { showFilterSheet = false },
            s = s
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// RICH WG CARD
// ═══════════════════════════════════════════════════════════════
@Composable
private fun WGRichCard(wg: WGPreview, s: AppStrings, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Name + distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    wg.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                DistanceBadge(wg.distanceKm)
            }

            Spacer(Modifier.height(12.dp))

            // Tags
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                wg.tags.forEach { tag ->
                    WGChip(tag.emoji, tag.label)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Avatars + ratio + details link
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Overlapping avatars
                Box(modifier = Modifier.height(32.dp)) {
                    wg.members.forEachIndexed { i, member ->
                        Surface(
                            shape = CircleShape,
                            color = member.color,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .size(32.dp)
                                .offset(x = (i * 22).dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    member.initials,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width((wg.members.size * 22 + 8).dp))

                Text(
                    "${wg.members.size}/${wg.capacity}",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 13.sp
                )

                Spacer(Modifier.weight(1f))

                Text(
                    "${s.viewDetails} →",
                    color = WGBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onClick)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// DISTANCE BADGE
// ═══════════════════════════════════════════════════════════════
@Composable
private fun DistanceBadge(km: Float) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Text(
            "${km}km",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// TAG CHIP
// ═══════════════════════════════════════════════════════════════
@Composable
private fun WGChip(emoji: String, label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 12.sp)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// DETAIL MODAL
// ═══════════════════════════════════════════════════════════════
@Composable
private fun WGDetailModal(
    wg: WGPreview,
    s: AppStrings,
    onDismiss: () -> Unit,
    onSendRequest: (String) -> Unit
) {
    var requestMessage by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column {
                // ─── Gradient Banner ────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF3B82F6), Color(0xFF7C3AED), Color(0xFFA855F7))
                            ),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                ) {
                    // Close button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.3f),
                        modifier = Modifier
                            .padding(12.dp)
                            .size(32.dp)
                            .align(Alignment.TopEnd)
                            .clickable(onClick = onDismiss)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // ─── Overlapping avatars on banner edge ─────────
                Box(
                    modifier = Modifier
                        .offset(x = 20.dp, y = (-20).dp)
                        .height(40.dp)
                ) {
                    wg.members.forEachIndexed { i, member ->
                        Surface(
                            shape = CircleShape,
                            color = member.color,
                            border = BorderStroke(3.dp, MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .size(40.dp)
                                .offset(x = (i * 28).dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    member.initials,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ─── Content below banner ───────────────────────
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .offset(y = (-8).dp)
                ) {
                    // Name + price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                wg.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${wg.distanceKm}km ${s.kmAway}",
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${wg.pricePerRoom}€",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                s.perRoom,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Tags
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        wg.tags.forEach { tag -> WGChip(tag.emoji, tag.label) }
                    }

                    Spacer(Modifier.height(20.dp))

                    // About
                    Text(
                        s.aboutUs,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        wg.aboutText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    // Members list
                    if (wg.members.isNotEmpty()) {
                        Text(
                            s.membersList,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        wg.members.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = member.color,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            member.initials,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    member.name.ifBlank { member.initials },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Amenities
                    Text(
                        s.amenitiesLabel,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        wg.amenities.forEach { amenity ->
                            AmenityTile(amenity)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Message field
                    OutlinedTextField(
                        value = requestMessage,
                        onValueChange = { requestMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 2,
                        maxLines = 3,
                        placeholder = {
                            Text(
                                s.yourMessage,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 13.sp
                            )
                        },
                        label = {
                            Text(s.requestMessage, color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedBorderColor = WGBlue,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            cursorColor = WGBlue,
                            unfocusedLabelColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = WGBlue
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // CTA
                    Button(
                        onClick = { onSendRequest(requestMessage) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WGBlueBright),
                        border = BorderStroke(1.dp, Color(0xFF60A5FA).copy(alpha = 0.4f))
                    ) {
                        Text(
                            s.sendRequest,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// AMENITY TILE
// ═══════════════════════════════════════════════════════════════
@Composable
private fun AmenityTile(amenity: WGAmenity) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(amenity.icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(amenity.label, color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
    }
}

// ═══════════════════════════════════════════════════════════════
// FILTER DIALOG
// ═══════════════════════════════════════════════════════════════
@Composable
private fun FilterDialog(
    filterParty: Boolean,
    filterQuiet: Boolean,
    onFilterPartyChange: (Boolean) -> Unit,
    onFilterQuietChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    s: AppStrings
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        s.filter,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onDismiss)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                FilterToggleRow("🎉 Party / Nightlife", filterParty, onFilterPartyChange)
                Spacer(Modifier.height(12.dp))
                FilterToggleRow("📚 Quiet / Study", filterQuiet, onFilterQuietChange)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WGBlueBright)
                ) {
                    Text(s.done, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FilterToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (checked) WGBlueBright.copy(alpha = 0.12f) else MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (checked) WGBlue.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChecked(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onChecked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = WGBlueBright,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
}
