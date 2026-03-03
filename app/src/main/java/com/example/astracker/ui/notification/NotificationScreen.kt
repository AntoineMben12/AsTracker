package com.example.astracker.ui.notification

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.network.models.NotificationDto
import com.example.astracker.network.models.NotificationGroupsDto
import com.example.astracker.ui.common.UiState
import com.example.astracker.ui.theme.AsTrackerTheme

// ── Colour tokens ──────────────────────────────────────────────────────────────
private val Primary      = Color(0xFF6366F1)   // Indigo-500
private val Secondary    = Color(0xFFF97316)   // Orange-400
private val Green        = Color(0xFF10B981)   // Emerald-500
private val Blue         = Color(0xFF2563EB)   // Blue-600
private val Purple       = Color(0xFF7C3AED)   // Violet-600
private val Teal         = Color(0xFF0D9488)   // Teal-600
private val Red          = Color(0xFFEF4444)   // Red-500
private val BgLight      = Color(0xFFF3F4F6)
private val BgDark       = Color(0xFF111827)
private val SrfLight     = Color(0xFFFFFFFF)
private val SrfDark      = Color(0xFF1F2937)
private val CardDark     = Color(0xFF253047)
private val TxtLight     = Color(0xFF111827)
private val TxtDark      = Color(0xFFF9FAFB)
private val TxtMuted     = Color(0xFF6B7280)
private val TxtMutedDark = Color(0xFF9CA3AF)

// ── Filter tabs ────────────────────────────────────────────────────────────────
private enum class NotifFilter { ALL, UNREAD, ASSIGNMENTS }

// ── Domain model ───────────────────────────────────────────────────────────────
enum class NotifGroup { TODAY, YESTERDAY, EARLIER }

data class NotifAction(val label: String, val isPrimary: Boolean)

data class NotifItem(
    val id            : String,
    val title         : String,
    val body          : String,
    val boldWord      : String        = "",
    val timeLabel     : String,
    val iconBg        : Color,
    val iconTint      : Color,
    val icon          : ImageVector,
    val hasUnreadDot  : Boolean       = false,
    val unreadDotColor: Color         = Secondary,
    val alpha         : Float         = 1f,
    val group         : NotifGroup,
    val type          : String        = "general",
    val actions       : List<NotifAction> = emptyList()
)

// ── Type → icon / colours ──────────────────────────────────────────────────────
private data class NotifStyle(
    val iconBg   : Color,
    val iconTint : Color,
    val icon     : ImageVector,
    val dotColor : Color
)

private fun styleForType(type: String): NotifStyle = when (type) {
    "new_assignment" -> NotifStyle(
        iconBg   = Color(0xFFDBEAFE),
        iconTint = Blue,
        icon     = Icons.Rounded.Add,
        dotColor = Blue
    )
    "completed"      -> NotifStyle(
        iconBg   = Color(0xFFD1FAE5),
        iconTint = Green,
        icon     = Icons.Rounded.CheckCircle,
        dotColor = Green
    )
    "deadline"       -> NotifStyle(
        iconBg   = Color(0xFFE0E7FF),
        iconTint = Primary,
        icon     = Icons.Rounded.PriorityHigh,
        dotColor = Primary
    )
    "grade"          -> NotifStyle(
        iconBg   = Color(0xFFD1FAE5),
        iconTint = Green,
        icon     = Icons.Rounded.Grade,
        dotColor = Green
    )
    "reminder"       -> NotifStyle(
        iconBg   = Color(0xFFFFEDD5),
        iconTint = Secondary,
        icon     = Icons.Rounded.Schedule,
        dotColor = Secondary
    )
    "cancellation"   -> NotifStyle(
        iconBg   = Color(0xFFEDE9FE),
        iconTint = Purple,
        icon     = Icons.Rounded.Campaign,
        dotColor = Purple
    )
    "submission"     -> NotifStyle(
        iconBg   = Color(0xFFCCFBF1),
        iconTint = Teal,
        icon     = Icons.Rounded.CloudUpload,
        dotColor = Teal
    )
    "overdue"        -> NotifStyle(
        iconBg   = Color(0xFFFEE2E2),
        iconTint = Red,
        icon     = Icons.Rounded.ErrorOutline,
        dotColor = Red
    )
    else             -> NotifStyle(
        iconBg   = Color(0xFFF3F4F6),
        iconTint = TxtMuted,
        icon     = Icons.Rounded.Notifications,
        dotColor = TxtMuted
    )
}

// ── Time helpers ───────────────────────────────────────────────────────────────
private fun formatTimeLabel(createdAt: String, group: NotifGroup): String {
    if (createdAt.length < 16) return ""
    return when (group) {
        NotifGroup.TODAY -> {
            val raw  = createdAt.substring(11, 16)
            val p    = raw.split(":")
            val h    = p[0].toIntOrNull() ?: 0
            val m    = p.getOrNull(1)?.toIntOrNull() ?: 0
            val ampm = if (h >= 12) "PM" else "AM"
            val h12  = if (h % 12 == 0) 12 else h % 12
            "%d:%02d %s".format(h12, m, ampm)
        }
        NotifGroup.YESTERDAY -> "Yesterday"
        NotifGroup.EARLIER   -> {
            val dp = createdAt.take(10).split("-")
            if (dp.size == 3) {
                val months = listOf("Jan","Feb","Mar","Apr","May","Jun",
                    "Jul","Aug","Sep","Oct","Nov","Dec")
                val mo = (dp[1].toIntOrNull() ?: 1) - 1
                val d  = dp[2].toIntOrNull() ?: 0
                "${months.getOrElse(mo) { "" }} $d"
            } else createdAt.take(10)
        }
    }
}

// ── DTO → UI model ─────────────────────────────────────────────────────────────
private fun NotificationDto.toNotifItem(group: NotifGroup): NotifItem {
    val style = styleForType(type)
    val alpha = when (group) {
        NotifGroup.TODAY     -> 1f
        NotifGroup.YESTERDAY -> 0.82f
        NotifGroup.EARLIER   -> 0.65f
    }
    return NotifItem(
        id             = id,
        title          = title,
        body           = body,
        boldWord       = boldWord,
        timeLabel      = formatTimeLabel(createdAt, group),
        iconBg         = style.iconBg,
        iconTint       = style.iconTint,
        icon           = style.icon,
        hasUnreadDot   = !isRead,
        unreadDotColor = style.dotColor,
        alpha          = alpha,
        group          = group,
        type           = type,
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
    NotifGroup.EARLIER   -> "Earlier"
}

// ── Assignment-related types (used by ASSIGNMENTS filter) ──────────────────────
private val assignmentTypes = setOf("new_assignment", "completed", "deadline", "submission", "overdue")

// ── Screen ─────────────────────────────────────────────────────────────────────
@Composable
fun NotificationScreen(
    isDarkTheme          : Boolean              = false,
    viewModel            : NotificationViewModel = NotificationViewModel(),
    onBack               : () -> Unit           = {},
    onNavigateToTasks    : () -> Unit           = {},
    onNavigateToCalendar : () -> Unit           = {},
    onNavigateToProfile  : () -> Unit           = {}
) {
    val bg    = if (isDarkTheme) BgDark       else BgLight
    val srf   = if (isDarkTheme) SrfDark      else SrfLight
    val txt   = if (isDarkTheme) TxtDark      else TxtLight
    val muted = if (isDarkTheme) TxtMutedDark else TxtMuted

    val notifState  by viewModel.state.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    var activeFilter by remember { mutableStateOf(NotifFilter.ALL) }

    // Reload on screen entry (picks up notifications pushed while away)
    LaunchedEffect(Unit) { viewModel.load() }

    // Map server → UI model (derived directly — no remember, so always in sync)
    val allItems: List<NotifItem> = when (val s = notifState) {
        is UiState.Success -> s.data.toNotifItems()
        else               -> emptyList()
    }

    // Apply active filter
    val notifItems: List<NotifItem> = when (activeFilter) {
        NotifFilter.ALL         -> allItems
        NotifFilter.UNREAD      -> allItems.filter { it.hasUnreadDot }
        NotifFilter.ASSIGNMENTS -> allItems.filter { it.type in assignmentTypes }
    }

    AsTrackerTheme(darkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
        ) {
            // ── Header ─────────────────────────────────────────────────────────
            NotifHeader(
                isDark      = isDarkTheme,
                srf         = srf,
                txt         = txt,
                muted       = muted,
                unreadCount = unreadCount,
                onBack      = onBack,
                onMarkAll   = { viewModel.markAllRead() }
            )

            // ── Filter tab bar ─────────────────────────────────────────────────
            FilterTabBar(
                active   = activeFilter,
                onSelect = { activeFilter = it },
                unread   = unreadCount,
                isDark   = isDarkTheme,
                srf      = srf
            )

            // ── List ───────────────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding    = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // Loading
                when (val s = notifState) {
                    is UiState.Loading -> item {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color    = Primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "Loading notifications…",
                                    color    = muted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    is UiState.Error -> item {
                        NotifErrorCard(
                            message  = s.message,
                            isDark   = isDarkTheme,
                            srf      = srf,
                            onRetry  = { viewModel.load() }
                        )
                    }
                    else -> Unit
                }

                // Grouped items
                NotifGroup.values().forEach { group ->
                    val groupItems = notifItems.filter { it.group == group }
                    if (groupItems.isEmpty()) return@forEach

                    item(key = group.name + "_header") {
                        NotifGroupHeader(
                            label  = groupLabel(group),
                            count  = groupItems.size,
                            isFirst = group == NotifGroup.TODAY,
                            muted  = muted
                        )
                    }

                    items(groupItems, key = { it.id.ifBlank { it.title + it.timeLabel } }) { item ->
                        NotifCard(
                            item      = item,
                            isDark    = isDarkTheme,
                            srf       = srf,
                            txt       = txt,
                            muted     = muted,
                            onMarkRead = { viewModel.markRead(item.id) },
                            onDelete   = { viewModel.delete(item.id) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                // Empty state — show when loaded successfully but list is empty
                if ((notifState is UiState.Success || notifState is UiState.Idle) && notifItems.isEmpty()) {
                    item {
                        NotifEmptyState(
                            filter = activeFilter,
                            isDark = isDarkTheme,
                            srf    = srf,
                            muted  = muted
                        )
                    }
                }
            }

            // ── Bottom nav ─────────────────────────────────────────────────────
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

// ── Header ─────────────────────────────────────────────────────────────────────
@Composable
private fun NotifHeader(
    isDark      : Boolean,
    srf         : Color,
    txt         : Color,
    muted       : Color,
    unreadCount : Int,
    onBack      : () -> Unit,
    onMarkAll   : () -> Unit
) {
    Surface(
        color           = srf,
        shadowElevation = 4.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = txt
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Notifications",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = txt
                )
                if (unreadCount > 0) {
                    Text(
                        "$unreadCount unread",
                        fontSize = 11.sp,
                        color    = Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = onMarkAll,
                enabled = unreadCount > 0
            ) {
                Icon(
                    Icons.Rounded.DoneAll,
                    contentDescription = "Mark all read",
                    tint = if (unreadCount > 0) Primary else muted
                )
            }
        }
    }
}

// ── Filter tab bar ─────────────────────────────────────────────────────────────
@Composable
private fun FilterTabBar(
    active   : NotifFilter,
    onSelect : (NotifFilter) -> Unit,
    unread   : Int,
    isDark   : Boolean,
    srf      : Color
) {
    Surface(
        color = srf,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier              = Modifier.fillMaxWidth(),
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Triple(NotifFilter.ALL,         "All",         null as Int?),
                Triple(NotifFilter.UNREAD,       "Unread",      if (unread > 0) unread else null),
                Triple(NotifFilter.ASSIGNMENTS,  "Assignments", null)
            )
            items(tabs) { (filter, label, badge) ->
                val isActive = active == filter
                val bgColor  by animateColorAsState(
                    if (isActive) Primary else Color.Transparent,
                    tween(200), label = "tab_bg_$label"
                )
                val txtColor by animateColorAsState(
                    when {
                        isActive   -> Color.White
                        isDark     -> TxtMutedDark
                        else       -> TxtMuted
                    },
                    tween(200), label = "tab_txt_$label"
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isActive) bgColor
                            else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
                        )
                        .clickable { onSelect(filter) }
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        label,
                        fontSize   = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color      = txtColor
                    )
                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    if (isActive) Color.White.copy(0.3f) else Secondary,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                badge.toString(),
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (isActive) Color.White else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
    HorizontalDivider(
        color     = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB),
        thickness = 0.5.dp
    )
}

// ── Group header ───────────────────────────────────────────────────────────────
@Composable
private fun NotifGroupHeader(
    label   : String,
    count   : Int,
    isFirst : Boolean,
    muted   : Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start  = 4.dp,
                end    = 4.dp,
                top    = if (isFirst) 0.dp else 20.dp,
                bottom = 10.dp
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text          = label.uppercase(),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color         = muted
        )
        Text(
            text      = "$count",
            fontSize  = 11.sp,
            color     = muted,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Notification card ──────────────────────────────────────────────────────────
@Composable
private fun NotifCard(
    item       : NotifItem,
    isDark     : Boolean,
    srf        : Color,
    txt        : Color,
    muted      : Color,
    onMarkRead : () -> Unit = {},
    onDelete   : () -> Unit = {}
) {
    val cardBg = when {
        item.hasUnreadDot && isDark  -> CardDark
        item.hasUnreadDot && !isDark -> Color(0xFFF0F4FF)
        else                          -> srf
    }

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = item.alpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.hasUnreadDot) 3.dp else 1.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.hasUnreadDot) { onMarkRead() }
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Icon ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        item.iconBg.copy(alpha = if (isDark) 0.25f else 1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = null,
                    tint               = item.iconTint,
                    modifier           = Modifier.size(22.dp)
                )
            }

            // ── Content ───────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {

                // Title + time
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        item.title,
                        fontSize   = 14.sp,
                        fontWeight = if (item.hasUnreadDot) FontWeight.Bold else FontWeight.SemiBold,
                        color      = txt.copy(alpha = item.alpha),
                        modifier   = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.timeLabel,
                        fontSize = 11.sp,
                        color    = muted.copy(alpha = item.alpha)
                    )
                }

                Spacer(Modifier.height(3.dp))

                // Body with optional bold span
                val bodyText = buildAnnotatedString {
                    if (item.boldWord.isNotBlank()) {
                        val parts = item.body.split(item.boldWord)
                        parts.forEachIndexed { idx, part ->
                            append(part)
                            if (idx < parts.size - 1) {
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        color      = txt.copy(alpha = item.alpha)
                                    )
                                ) { append(item.boldWord) }
                            }
                        }
                    } else {
                        append(item.body)
                    }
                }
                Text(
                    bodyText,
                    fontSize   = 13.sp,
                    color      = muted.copy(alpha = item.alpha),
                    lineHeight = 19.sp
                )

                // Type chip
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    TypeChip(type = item.type, isDark = isDark)

                    // Action buttons
                    if (item.actions.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item.actions.take(2).forEach { action ->
                                if (action.isPrimary) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Primary)
                                            .clickable { onMarkRead() }
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            action.label,
                                            color      = Color.White,
                                            fontSize   = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
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
                    } else {
                        // Delete button aligned right when no actions
                        IconButton(
                            onClick  = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete",
                                tint     = muted.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Delete alongside action buttons if actions present
                if (item.actions.isNotEmpty()) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick  = onDelete,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete",
                                tint     = muted.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ── Unread dot ────────────────────────────────────────────────────
            if (item.hasUnreadDot) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(9.dp)
                        .background(item.unreadDotColor, CircleShape)
                )
            }
        }
    }
}

// ── Type chip ──────────────────────────────────────────────────────────────────
@Composable
private fun TypeChip(type: String, isDark: Boolean) {
    val (label, color) = when (type) {
        "new_assignment" -> "New Assignment" to Blue
        "completed"      -> "Completed"      to Green
        "deadline"       -> "Deadline"       to Primary
        "grade"          -> "Grade"          to Green
        "reminder"       -> "Reminder"       to Secondary
        "cancellation"   -> "Update"         to Purple
        "submission"     -> "Submitted"      to Teal
        "overdue"        -> "Overdue"        to Red
        else             -> "General"        to TxtMuted
    }
    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = if (isDark) 0.18f else 0.10f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            label.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            color         = color,
            letterSpacing = 0.6.sp
        )
    }
}

// ── Empty state ────────────────────────────────────────────────────────────────
@Composable
private fun NotifEmptyState(
    filter : NotifFilter,
    isDark : Boolean,
    srf    : Color,
    muted  : Color
) {
    val (icon, headline, sub) = when (filter) {
        NotifFilter.ALL -> Triple(
            Icons.Rounded.NotificationsNone,
            "All caught up!",
            "No notifications yet. They'll appear here when you create or complete assignments."
        )
        NotifFilter.UNREAD -> Triple(
            Icons.Rounded.DoneAll,
            "Nothing unread",
            "You're fully up to date — no unread notifications."
        )
        NotifFilter.ASSIGNMENTS -> Triple(
            Icons.Rounded.Assignment,
            "No assignment alerts",
            "Notifications for new assignments and completions will show up here."
        )
    }

    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = srf),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier  = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Primary.copy(alpha = if (isDark) 0.2f else 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint     = Primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Text(
                    headline,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (isDark) TxtDark else TxtLight,
                    textAlign  = TextAlign.Center
                )
                Text(
                    sub,
                    fontSize  = 12.sp,
                    color     = muted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ── Error card ─────────────────────────────────────────────────────────────────
@Composable
private fun NotifErrorCard(
    message : String,
    isDark  : Boolean,
    srf     : Color,
    onRetry : () -> Unit = {}
) {
    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = srf),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint     = Red,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    message,
                    color      = Red,
                    fontSize   = 13.sp,
                    modifier   = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Red.copy(alpha = 0.10f))
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    "Retry",
                    color      = Red,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Bottom nav ─────────────────────────────────────────────────────────────────
@Composable
private fun NotifBottomNav(
    isDark               : Boolean,
    srf                  : Color,
    onNavigateToTasks    : () -> Unit,
    onNavigateToCalendar : () -> Unit,
    onNavigateToProfile  : () -> Unit = {}
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
            NotifNavItem(
                icon       = Icons.Rounded.Assignment,
                label      = "Tasks",
                isSelected = false,
                onClick    = onNavigateToTasks
            )
            NotifNavItem(
                icon       = Icons.Rounded.CalendarToday,
                label      = "Calendar",
                isSelected = false,
                onClick    = onNavigateToCalendar
            )
            NotifNavItem(
                icon       = Icons.Rounded.Notifications,
                label      = "Alerts",
                isSelected = true,
                hasBadge   = true
            )
            NotifNavItem(
                icon       = Icons.Rounded.Person,
                label      = "Profile",
                isSelected = false,
                onClick    = onNavigateToProfile
            )
        }
    }
}

@Composable
private fun NotifNavItem(
    icon       : ImageVector,
    label      : String,
    isSelected : Boolean,
    hasBadge   : Boolean  = false,
    onClick    : () -> Unit = {}
) {
    val tint = if (isSelected) Primary else TxtMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable(onClick = onClick)
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
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize   = 10.sp,
            color      = tint,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ── Previews ───────────────────────────────────────────────────────────────────
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
