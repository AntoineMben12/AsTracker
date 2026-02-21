package com.example.astracker.ui.notification

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.ui.theme.AsTrackerTheme

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val Primary   = Color(0xFF6366F1)   // Indigo-500
private val Secondary = Color(0xFFF97316)   // Orange-400
private val BgLight   = Color(0xFFF3F4F6)
private val BgDark    = Color(0xFF111827)
private val SrfLight  = Color(0xFFFFFFFF)
private val SrfDark   = Color(0xFF1F2937)
private val TxtLight  = Color(0xFF111827)
private val TxtDark   = Color(0xFFF9FAFB)
private val TxtMuted  = Color(0xFF6B7280)
private val TxtMutedDark = Color(0xFF9CA3AF)

// ── Domain model ──────────────────────────────────────────────────────────────
enum class NotifGroup { TODAY, YESTERDAY, EARLIER }

data class NotifAction(val label: String, val isPrimary: Boolean)

data class NotifItem(
    val id: Int,
    val title: String,
    val body: String,                    // plain text; use boldWord for one highlighted word/phrase
    val boldWord: String = "",
    val timeLabel: String,
    val iconBg: Color,
    val iconTint: Color,
    val icon: ImageVector,
    val hasUnreadDot: Boolean = false,
    val unreadDotColor: Color = Secondary,
    val alpha: Float = 1f,
    val group: NotifGroup,
    val actions: List<NotifAction> = emptyList()
)

private val notifications = listOf(
    NotifItem(
        id = 1,
        title  = "Assignment Due Soon",
        body   = " is due in 2 hours. Submit your work now to avoid penalties.",
        boldWord = "\"Database Systems Project\"",
        timeLabel = "2h ago",
        iconBg  = Color(0xFFE0E7FF),
        iconTint = Primary,
        icon    = Icons.Rounded.PriorityHigh,
        hasUnreadDot = true,
        unreadDotColor = Secondary,
        group   = NotifGroup.TODAY,
        actions = listOf(NotifAction("Submit Now", true))
    ),
    NotifItem(
        id = 2,
        title  = "New Assignment Added",
        body   = "Prof. Smith added a new assignment . Due date: Oct 25.",
        boldWord = "\"UX Research Paper\"",
        timeLabel = "5h ago",
        iconBg  = Color(0xFFDBEAFE),
        iconTint = Color(0xFF2563EB),
        icon    = Icons.Rounded.Add,
        hasUnreadDot = true,
        unreadDotColor = Primary,
        group   = NotifGroup.TODAY,
        actions = listOf(NotifAction("View Details", false))
    ),
    NotifItem(
        id = 3,
        title  = "Grade Posted",
        body   = "Your grade for  is available.",
        boldWord = "\"Mobile App Mockup\"",
        timeLabel = "8h ago",
        iconBg  = Color(0xFFD1FAE5),
        iconTint = Color(0xFF059669),
        icon    = Icons.Rounded.Grade,
        group   = NotifGroup.TODAY
    ),
    NotifItem(
        id = 4,
        title  = "Reminder: Team Meeting",
        body   = "Don't forget the group study session for Algorithm Analysis at 4:00 PM in the library.",
        timeLabel = "Yesterday",
        iconBg  = Color(0xFFFFEDD5),
        iconTint = Color(0xFFEA580C),
        icon    = Icons.Rounded.Schedule,
        alpha   = 0.75f,
        group   = NotifGroup.YESTERDAY
    ),
    NotifItem(
        id = 5,
        title  = "Class Cancelled",
        body   = "The lecture for  has been cancelled for tomorrow.",
        boldWord = "\"Introduction to AI\"",
        timeLabel = "Yesterday",
        iconBg  = Color(0xFFEDE9FE),
        iconTint = Color(0xFF7C3AED),
        icon    = Icons.Rounded.Campaign,
        alpha   = 0.75f,
        group   = NotifGroup.YESTERDAY
    ),
    NotifItem(
        id = 6,
        title  = "Submission Successful",
        body   = "\"Web Development Lab 3\" was uploaded successfully.",
        timeLabel = "Mon",
        iconBg  = Color(0xFFCCFBF1),
        iconTint = Color(0xFF0D9488),
        icon    = Icons.Rounded.CheckCircle,
        alpha   = 0.6f,
        group   = NotifGroup.EARLIER
    )
)

private fun groupLabel(group: NotifGroup) = when (group) {
    NotifGroup.TODAY     -> "Today"
    NotifGroup.YESTERDAY -> "Yesterday"
    NotifGroup.EARLIER   -> "Earlier This Week"
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun NotificationScreen(
    isDarkTheme: Boolean = false,
    onBack: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val bg  = if (isDarkTheme) BgDark  else BgLight
    val srf = if (isDarkTheme) SrfDark else SrfLight
    val txt = if (isDarkTheme) TxtDark else TxtLight
    val muted = if (isDarkTheme) TxtMutedDark else TxtMuted

    AsTrackerTheme(darkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
        ) {
            // ── Header ─────────────────────────────────────────────────────────
            Surface(
                color          = srf,
                shadowElevation = 4.dp,
                modifier       = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = txt
                        )
                    }
                    Text(
                        "Notifications",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = txt,
                        modifier   = Modifier.weight(1f),
                        textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = muted
                        )
                    }
                }
            }

            // ── Notification list ──────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                NotifGroup.values().forEach { group ->
                    val groupItems = notifications.filter { it.group == group }
                    if (groupItems.isEmpty()) return@forEach

                    item(key = group.name + "_header") {
                        Text(
                            text  = groupLabel(group).uppercase(),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = muted,
                            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp, top = if (group == NotifGroup.TODAY) 0.dp else 24.dp)
                        )
                    }

                    items(groupItems, key = { it.id }) { item ->
                        NotifCard(
                            item    = item,
                            isDark  = isDarkTheme,
                            srf     = srf,
                            txt     = txt,
                            muted   = muted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // ── Bottom nav ────────────────────────────────────────────────────
            NotifBottomNav(
                isDark               = isDarkTheme,
                srf                  = srf,
                onNavigateToTasks    = onNavigateToTasks,
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToProfile  = onNavigateToProfile
            )
        }
    }
}

// ── Notification card ─────────────────────────────────────────────────────────
@Composable
private fun NotifCard(
    item:  NotifItem,
    isDark: Boolean,
    srf:   Color,
    txt:   Color,
    muted: Color
) {
    val borderColor = if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)

    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = srf.copy(alpha = item.alpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                Modifier.background(Color.Transparent)
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(item.iconBg.copy(alpha = if (isDark) 0.3f else 1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = null,
                    tint               = item.iconTint,
                    modifier           = Modifier.size(22.dp)
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        item.title,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = txt.copy(alpha = item.alpha),
                        modifier   = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        item.timeLabel,
                        fontSize = 11.sp,
                        color    = muted.copy(alpha = item.alpha)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Body with optional bold span
                if (item.boldWord.isNotEmpty()) {
                    val bodyParts = item.body.split(item.boldWord.trim())
                    Text(
                        buildAnnotatedString {
                            // Insert bold word in the middle of the body intelligently
                            val insertIdx = item.body.indexOf(" ")
                            if (insertIdx > 0 && bodyParts.size == 1) {
                                append(item.body)
                            } else {
                                bodyParts.forEachIndexed { idx, part ->
                                    append(part)
                                    if (idx < bodyParts.size - 1) {
                                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = txt.copy(item.alpha))) {
                                            append(item.boldWord)
                                        }
                                    }
                                }
                            }
                        },
                        fontSize = 13.sp,
                        color    = muted.copy(alpha = item.alpha),
                        lineHeight = 19.sp
                    )
                } else {
                    Text(
                        item.body,
                        fontSize = 13.sp,
                        color    = muted.copy(alpha = item.alpha),
                        lineHeight = 19.sp
                    )
                }

                // Action buttons
                if (item.actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.actions.forEach { action ->
                            if (action.isPrimary) {
                                Button(
                                    onClick = {},
                                    colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape   = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(action.label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            } else {
                                Text(
                                    action.label,
                                    color      = Primary,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier   = Modifier.clickable {}
                                )
                            }
                        }
                    }
                }
            }

            // Unread dot
            if (item.hasUnreadDot) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(item.unreadDotColor, CircleShape)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

// ── Bottom nav (Alerts selected) ──────────────────────────────────────────────
@Composable
private fun NotifBottomNav(
    isDark: Boolean,
    srf: Color,
    onNavigateToTasks: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    Surface(
        color           = srf,
        shadowElevation = 16.dp,
        shape           = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier        = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            NotifNavItem(icon = Icons.Rounded.Assignment,    label = "Tasks",    isSelected = false, onClick = onNavigateToTasks)
            NotifNavItem(icon = Icons.Rounded.CalendarToday, label = "Calendar", isSelected = false, onClick = onNavigateToCalendar)
            NotifNavItem(icon = Icons.Rounded.Notifications, label = "Alerts",   isSelected = true,  hasBadge = true)
            NotifNavItem(icon = Icons.Rounded.Person,        label = "Profile",  isSelected = false,  onClick = onNavigateToProfile)
        }
    }
}

@Composable
private fun NotifNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    hasBadge: Boolean = false,
    onClick: () -> Unit = {}
) {
    val tint = if (isSelected) Primary else TxtMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = tint,
                modifier           = Modifier.size(24.dp)
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Secondary, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                )
            }
        }
        Text(label, fontSize = 10.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    NotificationScreen()
}

@Preview(showBackground = true, backgroundColor = 0xFF111827)
@Composable
fun NotificationScreenDarkPreview() {
    NotificationScreen(isDarkTheme = true)
}
