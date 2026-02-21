package com.example.astracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.ui.theme.AsTrackerTheme

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val Primary   = Color(0xFF6366F1)
private val Accent    = Color(0xFFF97316)
private val BgLight   = Color(0xFFF3F4F6)
private val BgDark    = Color(0xFF111827)
private val CardLight = Color(0xFFFFFFFF)
private val CardDark  = Color(0xFF1F2937)
private val TxtLight  = Color(0xFF1F2937)
private val TxtDark   = Color(0xFFF9FAFB)
private val TxtMuted  = Color(0xFF6B7280)
private val TxtMutedDark = Color(0xFF9CA3AF)

// ── Stat card data ────────────────────────────────────────────────────────────
private data class StatItem(
    val value: String,
    val label: String,
    val iconBg: Color,
    val iconTint: Color,
    val icon: ImageVector
)

private val stats = listOf(
    StatItem("42",  "Completed", Color(0xFFE0E7FF), Color(0xFF4F46E5), Icons.Rounded.CheckCircle),
    StatItem("5",   "Active",    Color(0xFFFFEDD5), Color(0xFFEA580C), Icons.Rounded.Assignment),
    StatItem("2",   "Overdue",   Color(0xFFEEF2FF), Color(0xFF6366F1), Icons.Rounded.PriorityHigh),
    StatItem("85%", "Avg Score", Color(0xFFFFF7ED), Color(0xFFF97316), Icons.Rounded.TrendingUp)
)

// ── Settings row data ─────────────────────────────────────────────────────────
private data class SettingRow(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = "",
    val isDanger: Boolean = false
)

private val accountSettings = listOf(
    SettingRow(Icons.Rounded.Person,        "Edit Profile",               "Update name, major, and photo"),
    SettingRow(Icons.Rounded.Notifications, "Notification Preferences",   "Manage push alerts and emails"),
    SettingRow(Icons.Rounded.Lock,          "Privacy & Security",         "Change password")
)

private val supportSettings = listOf(
    SettingRow(Icons.Rounded.Help,   "Help & Support"),
    SettingRow(Icons.Rounded.Logout, "Log Out", isDanger = true)
)

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean = false,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {}
) {
    val bg   = if (isDarkTheme) BgDark   else BgLight
    val card = if (isDarkTheme) CardDark else CardLight
    val txt  = if (isDarkTheme) TxtDark  else TxtLight
    val muted = if (isDarkTheme) TxtMutedDark else TxtMuted

    AsTrackerTheme(darkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
        ) {
            // ── Header ─────────────────────────────────────────────────────────
            Surface(
                color = card,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = txt)
                    }
                    Text(
                        "Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = txt,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(onClick = {}, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = muted)
                    }
                }
            }

            // ── Scrollable body ────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 24.dp, end = 24.dp,
                    top = 28.dp, bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Avatar + name ───────────────────────────────────────────────
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        // Avatar with gradient ring + edit badge
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Primary, Accent)
                                        )
                                    )
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color(0xFFE0E7FF), Color(0xFFEDE9FE))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                            }
                            // Edit badge
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Primary, CircleShape)
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = "Edit photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Alex Johnson",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = txt
                        )
                        Text(
                            "Computer Science Major",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = muted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isDarkTheme) Color(0xFF312E81).copy(0.3f) else Color(0xFFE0E7FF),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Year 3",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) Color(0xFFA5B4FC) else Color(0xFF4338CA)
                            )
                        }
                    }
                }

                // ── Stats grid ──────────────────────────────────────────────────
                item {
                    StatGrid(
                        stats = stats,
                        card = card,
                        txt = txt,
                        muted = muted,
                        isDark = isDarkTheme
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ── Account Settings ────────────────────────────────────────────
                item {
                    SettingsGroup(
                        title = "Account Settings",
                        rows = accountSettings,
                        card = card,
                        txt = txt,
                        muted = muted,
                        isDark = isDarkTheme,
                        onRowClick = {}
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Support + Logout ────────────────────────────────────────────
                item {
                    SettingsGroup(
                        title = null,
                        rows = supportSettings,
                        card = card,
                        txt = txt,
                        muted = muted,
                        isDark = isDarkTheme,
                        onRowClick = { row ->
                            if (row.isDanger) onLogout()
                        }
                    )
                }
            }

            // ── Bottom nav ────────────────────────────────────────────────────
            ProfileBottomNav(
                card = card,
                onNavigateToTasks = onNavigateToTasks,
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToNotification = onNavigateToNotification
            )
        }
    }
}

// ── Stat grid ─────────────────────────────────────────────────────────────────
@Composable
private fun StatGrid(
    stats: List<StatItem>,
    card: Color,
    txt: Color,
    muted: Color,
    isDark: Boolean
) {
    val rows = stats.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { stat ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        stat.iconBg.copy(if (isDark) 0.3f else 1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(stat.icon, null, tint = stat.iconTint, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stat.value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = txt)
                            Text(
                                stat.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.8.sp,
                                color = muted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                // If odd number of items, fill the remaining space
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ── Settings group ────────────────────────────────────────────────────────────
@Composable
private fun SettingsGroup(
    title: String?,
    rows: List<SettingRow>,
    card: Color,
    txt: Color,
    muted: Color,
    isDark: Boolean,
    onRowClick: (SettingRow) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (title != null) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = txt,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
                HorizontalDivider(color = if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6))
            }
            rows.forEachIndexed { idx, row ->
                SettingRowItem(
                    row = row,
                    card = card,
                    txt = txt,
                    muted = muted,
                    isDark = isDark,
                    onClick = { onRowClick(row) }
                )
                if (idx < rows.size - 1) {
                    HorizontalDivider(color = if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6))
                }
            }
        }
    }
}

@Composable
private fun SettingRowItem(
    row: SettingRow,
    card: Color,
    txt: Color,
    muted: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val iconBg   = if (row.isDanger) Color(0xFFFFE4E6).copy(if (isDark) 0.3f else 1f)
                   else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
    val iconTint = if (row.isDanger) Color(0xFFDC2626)
                   else if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
    val titleColor = if (row.isDanger) Color(0xFFDC2626) else txt

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBg, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(row.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(row.title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = titleColor)
            if (row.subtitle.isNotEmpty()) {
                Text(row.subtitle, fontSize = 12.sp, color = muted)
            }
        }
        if (!row.isDanger) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = muted, modifier = Modifier.size(20.dp))
        }
    }
}

// ── Bottom nav (Profile tab selected) ────────────────────────────────────────
@Composable
private fun ProfileBottomNav(
    card: Color,
    onNavigateToTasks: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToNotification: () -> Unit
) {
    Surface(
        color = card,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileNavItem(icon = Icons.Rounded.Assignment,    label = "Tasks",    isSelected = false, onClick = onNavigateToTasks)
            ProfileNavItem(icon = Icons.Rounded.CalendarToday, label = "Calendar", isSelected = false, onClick = onNavigateToCalendar)
            ProfileNavItem(icon = Icons.Rounded.Notifications, label = "Alerts",   isSelected = false, onClick = onNavigateToNotification)
            ProfileNavItem(icon = Icons.Rounded.Person,        label = "Profile",  isSelected = true)
        }
    }
}

@Composable
private fun ProfileNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) Primary else TxtMuted,
            modifier = Modifier.size(24.dp)
        )
        Text(label, fontSize = 10.sp, color = if (isSelected) Primary else TxtMuted, fontWeight = FontWeight.Medium)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}

@Preview(showBackground = true, backgroundColor = 0xFF111827)
@Composable
fun ProfileScreenDarkPreview() {
    ProfileScreen(isDarkTheme = true)
}
