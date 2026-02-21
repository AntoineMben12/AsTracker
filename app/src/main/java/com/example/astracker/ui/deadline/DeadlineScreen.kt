package com.example.astracker.ui.deadline

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.ui.assignment.PrimaryColor
import com.example.astracker.ui.theme.AsTrackerTheme

// Colour tokens ─────────────────────────────────────────────────────────────
private val Urgent      = Color(0xFFEF4444)   // Red-500
private val Secondary   = Color(0xFFFB923C)   // Orange-400
private val Success     = Color(0xFF10B981)   // Emerald-500
private val BgLight     = Color(0xFFF3F4F6)
private val BgDark      = Color(0xFF111827)
private val SrfLight    = Color(0xFFFFFFFF)
private val SrfDark     = Color(0xFF1F2937)
private val TxtLight    = Color(0xFF1F2937)
private val TxtDark     = Color(0xFFF9FAFB)
private val TxtMuted    = Color(0xFF6B7280)

// ── Domain models ─────────────────────────────────────────────────────────────
enum class DeadlineStatus { TODAY, TOMORROW, UPCOMING, NEUTRAL }

data class DeadlineItem(
    val subject: String,
    val description: String,
    val status: DeadlineStatus,
    val timeLabel: String,
    val dateLabel: String,
    val accentColor: Color,
    val icon: ImageVector,
    val tags: List<String> = emptyList(),
    val progress: Float = -1f       // -1 means no progress bar
)

private val sampleDeadlines = listOf(
    DeadlineItem(
        subject     = "Database Systems",
        description = "Submit ER Diagram and Schema SQL file.",
        status      = DeadlineStatus.TODAY,
        timeLabel   = "11:59 PM",
        dateLabel   = "Due Today",
        accentColor = Urgent,
        icon        = Icons.Rounded.PriorityHigh,
        progress    = 0.85f
    ),
    DeadlineItem(
        subject     = "Web Development",
        description = "Implement Login and Deadline View.",
        status      = DeadlineStatus.TOMORROW,
        timeLabel   = "10:00 AM",
        dateLabel   = "Tomorrow",
        accentColor = Secondary,
        icon        = Icons.Rounded.Code,
        tags        = listOf("Frontend", "HTML")
    ),
    DeadlineItem(
        subject     = "Calculus II Quiz",
        description = "Chapter 5: Integration Techniques.",
        status      = DeadlineStatus.UPCOMING,
        timeLabel   = "2:00 PM",
        dateLabel   = "Fri, Oct 27",
        accentColor = Success,
        icon        = Icons.Rounded.Calculate
    ),
    DeadlineItem(
        subject     = "History Essay",
        description = "First draft of research paper due.",
        status      = DeadlineStatus.NEUTRAL,
        timeLabel   = "",
        dateLabel   = "Mon, Oct 30",
        accentColor = Color(0xFF9CA3AF),
        icon        = Icons.Rounded.HistoryEdu
    )
)

// ── Mini calendar day data ────────────────────────────────────────────────────
private data class CalendarDay(
    val dayLabel: String,
    val date: Int,
    val dot: Color? = null,            // null = no dot
    val isSelected: Boolean = false
)

private val weekDays = listOf(
    CalendarDay("Mon", 23),
    CalendarDay("Tue", 24, dot = Secondary),
    CalendarDay("Wed", 25, dot = Urgent, isSelected = true),
    CalendarDay("Thu", 26),
    CalendarDay("Fri", 27, dot = Success),
    CalendarDay("Sat", 28)
)

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun DeadlineScreen(
    isDarkTheme: Boolean = false,
    onBack: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToAddAssignment: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val bg  = if (isDarkTheme) BgDark  else BgLight
    val srf = if (isDarkTheme) SrfDark else SrfLight
    val txt = if (isDarkTheme) TxtDark else TxtLight

    AsTrackerTheme(darkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Sticky header ──────────────────────────────────────────────
                DeadlineHeader(
                    isDark        = isDarkTheme,
                    srf           = srf,
                    txt           = txt,
                    onBack        = onBack
                )

                // ── Scrollable body ────────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 24.dp, end = 24.dp,
                        top = 24.dp, bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Section title
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                "Upcoming Deadlines",
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color      = txt
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isDarkTheme) Color(0xFF312E81).copy(0.4f)
                                        else Color(0xFFE0E7FF),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Today",
                                    color      = PrimaryColor,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Timeline items
                    items(sampleDeadlines) { item ->
                        TimelineItem(
                            item      = item,
                            isDark    = isDarkTheme,
                            srf       = srf,
                            txt       = txt,
                            isLast    = item == sampleDeadlines.last()
                        )
                    }
                }
            }

            // ── FAB ────────────────────────────────────────────────────────────
            FloatingActionButton(
                onClick          = onNavigateToAddAssignment,
                containerColor   = PrimaryColor,
                contentColor     = Color.White,
                shape            = CircleShape,
                modifier         = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 90.dp)
                    .size(56.dp)
                    .shadow(12.dp, CircleShape, spotColor = PrimaryColor.copy(0.4f))
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
            }

            // ── Bottom nav ────────────────────────────────────────────────────
            DeadlineBottomNav(
                isDark                   = isDarkTheme,
                srf                      = srf,
                onNavigateToTasks        = onNavigateToTasks,
                onNavigateToNotification = onNavigateToNotification,
                onNavigateToProfile      = onNavigateToProfile,
                modifier                 = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ── Header with mini-calendar ────────────────────────────────────────────────
@Composable
private fun DeadlineHeader(
    isDark: Boolean,
    srf: Color,
    txt: Color,
    onBack: () -> Unit
) {
    var viewMode by remember { mutableStateOf("Month") }

    Surface(
        color          = srf,
        shadowElevation = 4.dp,
        shape          = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        modifier       = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp, top = 8.dp)
        ) {
            // ── Top row ──────────────────────────────────────────────────────
            Row(
                modifier      = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = "Back",
                        tint       = TxtMuted,
                        modifier   = Modifier.size(28.dp)
                    )
                }
                Text(
                    "Schedule",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = txt
                )
                Box {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Rounded.Notifications,
                            contentDescription = "Notifications",
                            tint     = TxtMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Urgent badge
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Urgent, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Month + view toggle ───────────────────────────────────────────
            Row(
                modifier      = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "October 2023",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = txt
                    )
                    Text(
                        "5 Assignments due",
                        fontSize = 13.sp,
                        color    = TxtMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                // Month / Week toggle
                Row(
                    modifier = Modifier
                        .background(
                            if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("Month", "Week").forEach { mode ->
                        val isActive = viewMode == mode
                        val bg by animateColorAsState(
                            if (isActive) srf else Color.Transparent,
                            tween(200), label = "toggle_bg"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .clickable { viewMode = mode }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                mode,
                                fontSize   = 11.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color      = if (isActive) PrimaryColor else TxtMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Day strip ────────────────────────────────────────────────────
            Row(
                modifier      = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { day ->
                    DayCell(day = day, isDark = isDark)
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: CalendarDay, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = day.dayLabel,
            fontSize   = 11.sp,
            fontWeight = if (day.isSelected) FontWeight.Bold else FontWeight.Medium,
            color = when {
                day.isSelected -> PrimaryColor
                isDark         -> Color(0xFF9CA3AF)
                else           -> TxtMuted
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .then(
                    if (day.isSelected)
                        Modifier
                            .shadow(8.dp, CircleShape, spotColor = PrimaryColor.copy(0.3f))
                            .background(PrimaryColor, CircleShape)
                    else
                        Modifier.background(Color.Transparent, CircleShape)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = day.date.toString(),
                fontSize   = 15.sp,
                fontWeight = if (day.isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = when {
                    day.isSelected -> Color.White
                    isDark         -> Color(0xFFD1D5DB)
                    else           -> Color(0xFF4B5563)
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(day.dot ?: Color.Transparent, CircleShape)
        )
    }
}

// ── Timeline item ─────────────────────────────────────────────────────────────
@Composable
private fun TimelineItem(
    item:   DeadlineItem,
    isDark: Boolean,
    srf:    Color,
    txt:    Color,
    isLast: Boolean
) {
    val dimmed = item.status == DeadlineStatus.NEUTRAL
    val cardAlpha = if (dimmed) 0.7f else 1f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeline column: icon + line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.accentColor.copy(alpha = 0.12f), CircleShape)
                    .then(
                        Modifier.clip(CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent, CircleShape)
                        .then(
                            Modifier.clip(CircleShape)
                        )
                ) {
                    Surface(
                        shape = CircleShape,
                        color = item.accentColor.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, item.accentColor),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector        = item.icon,
                                contentDescription = null,
                                tint               = item.accentColor,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(
                            if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                        )
                )
            }
        }

        // Card
        Card(
            shape  = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = srf.copy(alpha = cardAlpha)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Status row
                Row(
                    modifier      = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    StatusBadge(item = item, isDark = isDark)
                    if (item.timeLabel.isNotEmpty()) {
                        Text(item.timeLabel, fontSize = 11.sp, color = TxtMuted, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    item.subject,
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color      = txt.copy(alpha = cardAlpha)
                )
                Text(
                    item.description,
                    fontSize  = 13.sp,
                    color     = TxtMuted.copy(alpha = cardAlpha),
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis,
                    modifier  = Modifier.padding(top = 4.dp)
                )

                // Progress bar
                if (item.progress >= 0f) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress           = { item.progress },
                        modifier           = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color              = item.accentColor,
                        trackColor         = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                    )
                }

                // Tags
                if (item.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    tag,
                                    fontSize = 11.sp,
                                    color    = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(item: DeadlineItem, isDark: Boolean) {
    val bg = when (item.status) {
        DeadlineStatus.TODAY    -> if (isDark) Color(0xFF7F1D1D).copy(0.4f) else Color(0xFFFEF2F2)
        DeadlineStatus.TOMORROW -> if (isDark) Color(0xFF7C2D12).copy(0.4f) else Color(0xFFFFF7ED)
        DeadlineStatus.UPCOMING -> if (isDark) Color(0xFF064E3B).copy(0.4f) else Color(0xFFECFDF5)
        DeadlineStatus.NEUTRAL  -> if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            item.dateLabel.uppercase(),
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            color      = item.accentColor,
            letterSpacing = 0.8.sp
        )
    }
}

// ── Bottom nav (Calendar tab selected) ───────────────────────────────────────
@Composable
private fun DeadlineBottomNav(
    isDark: Boolean,
    srf: Color,
    onNavigateToTasks: () -> Unit,
    onNavigateToNotification: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color          = srf,
        shadowElevation = 16.dp,
        shape          = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier       = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Tasks
            DeadlineNavItem(
                icon       = Icons.Rounded.Assignment,
                label      = "Tasks",
                isSelected = false,
                onClick    = onNavigateToTasks
            )
            // Calendar (selected)
            DeadlineNavItem(
                icon       = Icons.Rounded.CalendarToday,
                label      = "Calendar",
                isSelected = true,
                onClick    = {}
            )
            // Alerts
            DeadlineNavItem(
                icon       = Icons.Rounded.Notifications,
                label      = "Alerts",
                isSelected = false,
                hasBadge   = true,
                onClick    = onNavigateToNotification
            )
            // Profile
            DeadlineNavItem(
                icon       = Icons.Rounded.Person,
                label      = "Profile",
                isSelected = false,
                onClick    = onNavigateToProfile
            )
        }
    }
}

@Composable
private fun DeadlineNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    hasBadge: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = if (isSelected) PrimaryColor else TxtMuted,
                modifier           = Modifier.size(24.dp)
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                )
            }
        }
        Text(
            text       = label,
            fontSize   = 10.sp,
            color      = if (isSelected) PrimaryColor else TxtMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun DeadlineScreenPreview() {
    DeadlineScreen()
}

@Preview(showBackground = true)
@Composable
fun DeadlineScreenDarkPreview() {
    DeadlineScreen(isDarkTheme = true)
}
