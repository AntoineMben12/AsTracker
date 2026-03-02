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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.network.models.NotificationDto
import com.example.astracker.network.models.NotificationGroupsDto
import com.example.astracker.ui.common.UiState
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

// ── DTO → UI model conversion ─────────────────────────────────────────────────

private fun iconForType(type: String): Triple<Color, Color, ImageVector> = when (type) {
    "deadline"       -> Triple(Color(0xFFE0E7FF), Primary,            Icons.Rounded.PriorityHigh)
    "new_assignment" -> Triple(Color(0xFFDBEAFE), Color(0xFF2563EB),  Icons.Rounded.Add)
    "grade"          -> Triple(Color(0xFFD1FAE5), Color(0xFF059669),  Icons.Rounded.Grade)
    "reminder"       -> Triple(Color(0xFFFFEDD5), Color(0xFFEA580C),  Icons.Rounded.Schedule)
    "cancellation"   -> Triple(Color(0xFFEDE9FE), Color(0xFF7C3AED),  Icons.Rounded.Campaign)
    "submission"     -> Triple(Color(0xFFCCFBF1), Color(0xFF0D9488),  Icons.Rounded.CheckCircle)
    else             -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280),  Icons.Rounded.Notifications)
}

private fun formatTimeLabel(createdAt: String, group: NotifGroup): String {
    if (createdAt.length < 16) return ""
    return when (group) {
        NotifGroup.TODAY -> {
            val rawTime = createdAt.substring(11, 16) // "HH:mm"
            val parts   = rawTime.split(":")
            val h       = parts[0].toIntOrNull() ?: 0
            val m       = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val ampm    = if (h >= 12) "PM" else "AM"
            val h12     = if (h % 12 == 0) 12 else h % 12
            "%d:%02d %s".format(h12, m, ampm)
        }
        NotifGroup.YESTERDAY -> "Yesterday"
        NotifGroup.EARLIER   -> createdAt.take(10)
    }
}

private fun NotificationDto.toNotifItem(group: NotifGroup): NotifItem {
    val (iconBg, iconTint, icon) = iconForType(type)
    val alpha = when (group) {
        NotifGroup.TODAY     -> 1f
        NotifGroup.YESTERDAY -> 0.75f
        NotifGroup.EARLIER   -> 0.6f
    }
    return NotifItem(
        id             = id.hashCode(),
        title          = title,
        body           = body,
        boldWord       = boldWord,
        timeLabel      = formatTimeLabel(createdAt, group),
        iconBg         = iconBg,
        iconTint       = iconTint,
        icon           = icon,
        hasUnreadDot   = !isRead,
        unreadDotColor = if (group == NotifGroup.TODAY) Secondary else Primary,
        alpha          = alpha,
        group          = group,
        actions        = actions.map { NotifAction(it.label, it.isPrimary) }
    )
}

private fun NotificationGroupsDto.toNotifItems(): List<NotifItem> =
    today.map     { it.toNotifItem(NotifGroup.TODAY)     } +
    yesterday.map { it.toNotifItem(NotifGroup.YESTERDAY) } +
    earlier.map   { it.toNotifItem(NotifGroup.EARLIER)   }

private fun groupLabel(group: NotifGroup) = when (group) {
    NotifGroup.TODAY     -> "Today"
    NotifGroup.YESTERDAY -> "Yesterday"
    NotifGroup.EARLIER   -> "Earlier This Week"
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun NotificationScreen(
    isDarkTheme: Boolean = false,
    viewModel: NotificationViewModel = NotificationViewModel(),
    onBack: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val bg    = if (isDarkTheme) BgDark       else BgLight
    val srf   = if (isDarkTheme) SrfDark      else SrfLight
    val txt   = if (isDarkTheme) TxtDark      else TxtLight
    val muted = if (isDarkTheme) TxtMutedDark else TxtMuted

    val notifState by viewModel.state.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    // Map server data to local UI model
    val notifItems: List<NotifItem> = when (val s = notifState) {
        is UiState.Success -> s.data.toNotifItems()
        else               -> emptyList()
    }

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
                    IconButton(onClick = { viewModel.markAllRead() }) {
                        Icon(
                            Icons.Rounded.DoneAll,
                            contentDescription = "Mark all read",
                            tint = if (unreadCount > 0) Primary else muted
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
                // Loading state
                when (val s = notifState) {
                    is UiState.Loading -> item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = Primary) }
                    }
                    is UiState.Error -> item {
                        Text(
                            s.message,
                            color    = Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    else -> Unit
                }

                NotifGroup.values().forEach { group ->
                    val groupItems = notifItems.filter { it.group == group }
                    if (groupItems.isEmpty()) return@forEach

                    item(key = group.name + "_header") {
                        Text(
                            text          = groupLabel(group).uppercase(),
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color         = muted,
                            modifier      = Modifier.padding(
                                start  = 8.dp,
                                bottom = 12.dp,
                                top    = if (group == NotifGroup.TODAY) 0.dp else 24.dp
                            )
                        )
                    }

                    items(groupItems, key = { it.id }) { item ->
                        NotifCard(
                            item     = item,
                            isDark   = isDarkTheme,
                            srf      = srf,
                            txt      = txt,
                            muted    = muted,
                            onMarkRead = { viewModel.markRead(
                                notifItems.find { ni -> ni.id == item.id }
                                    ?.let { ni ->
                                        when (item.group) {
                                            NotifGroup.TODAY ->
                                                (notifState as? UiState.Success)?.data?.today
                                                    ?.find { it.title == item.title }?.id ?: ""
                                            NotifGroup.YESTERDAY ->
                                                (notifState as? UiState.Success)?.data?.yesterday
                                                    ?.find { it.title == item.title }?.id ?: ""
                                            NotifGroup.EARLIER ->
                                                (notifState as? UiState.Success)?.data?.earlier
                                                    ?.find { it.title == item.title }?.id ?: ""
                                        }
                                    } ?: ""
                            )},
                            onDelete = { viewModel.delete(
                                when (item.group) {
                                    NotifGroup.TODAY ->
                                        (notifState as? UiState.Success)?.data?.today
                                            ?.find { it.title == item.title }?.id ?: ""
                                    NotifGroup.YESTERDAY ->
                                        (notifState as? UiState.Success)?.data?.yesterday
                                            ?.find { it.title == item.title }?.id ?: ""
                                    NotifGroup.EARLIER ->
                                        (notifState as? UiState.Success)?.data?.earlier
                                            ?.find { it.title == item.title }?.id ?: ""
                                }
                            )}
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Empty state
                if (notifState is UiState.Success && notifItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.NotificationsNone,
                                    contentDescription = null,
                                    tint     = muted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No notifications yet",
                                    color    = muted,
                                    fontSize = 14.sp
                                )
                            }
                        }
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
    item:       NotifItem,
    isDark:     Boolean,
    srf:        Color,
    txt:        Color,
    muted:      Color,
    onMarkRead: () -> Unit = {},
    onDelete:   () -> Unit = {}
) {
</thinking>
    val borderColor = if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)

    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = srf.copy(alpha = item.alpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.hasUnreadDot) { onMarkRead() }
    ) {
</thinking>
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
                                    onClick = { onMarkRead() },
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
                                    modifier   = Modifier.clickable { onMarkRead() }
                                )
                            }
                        }
                    }
                }
                // Delete row
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete notification",
                            tint     = muted,
                            modifier = Modifier.size(16.dp)
                        )
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
