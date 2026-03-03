package com.example.astracker.ui.deadline

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.network.models.AssignmentDto
import com.example.astracker.ui.assignment.AssignmentViewModel
import com.example.astracker.ui.assignment.PrimaryColor
import com.example.astracker.ui.common.UiState
import com.example.astracker.ui.theme.AsTrackerTheme
import java.util.Calendar

// ── Colour tokens ──────────────────────────────────────────────────────────────
private val Urgent    = Color(0xFFEF4444)
private val Secondary = Color(0xFFFB923C)
private val Success   = Color(0xFF10B981)
private val Purple    = Color(0xFF9333EA)
private val BgLight   = Color(0xFFF3F4F6)
private val BgDark    = Color(0xFF111827)
private val SrfLight  = Color(0xFFFFFFFF)
private val SrfDark   = Color(0xFF1F2937)
private val TxtLight  = Color(0xFF1F2937)
private val TxtDark   = Color(0xFFF9FAFB)
private val TxtMuted  = Color(0xFF6B7280)

// ── View mode ──────────────────────────────────────────────────────────────────
private enum class CalendarViewMode { WEEK, MONTH }

// ── Domain models ──────────────────────────────────────────────────────────────
enum class DeadlineStatus { TODAY, TOMORROW, UPCOMING, OVERDUE, NEUTRAL }

data class DeadlineItem(
    val id          : String       = "",
    val subject     : String,
    val description : String,
    val status      : DeadlineStatus,
    val timeLabel   : String,
    val dateLabel   : String,
    val accentColor : Color,
    val icon        : ImageVector,
    val tags        : List<String> = emptyList(),
    val progress    : Float        = -1f,
    val priority    : String       = "Medium",
    val isoDate     : String       = ""
)

// ── Calendar day model ─────────────────────────────────────────────────────────
private data class CalendarDay(
    val dayLabel    : String,
    val date        : Int,
    val isoDate     : String,
    val dots        : List<Color> = emptyList(),
    val isSelected  : Boolean     = false,
    val isToday     : Boolean     = false,
    val isCurrentMo : Boolean     = true
)

// ── Date helpers ───────────────────────────────────────────────────────────────
private fun calStr(cal: Calendar): String = "%04d-%02d-%02d".format(
    cal.get(Calendar.YEAR),
    cal.get(Calendar.MONTH) + 1,
    cal.get(Calendar.DAY_OF_MONTH)
)

private fun monthYearOf(cal: Calendar): String {
    val months = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )
    return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
}

private fun dotsForDay(iso: String, assignments: List<AssignmentDto>): List<Color> {
    val day = assignments.filter { it.dueDate.startsWith(iso) && it.status != "completed" }
    if (day.isEmpty()) return emptyList()
    val result = mutableListOf<Color>()
    if (day.any { it.priority == "High"   }) result += Urgent
    if (day.any { it.priority == "Medium" }) result += Secondary
    if (day.any { it.priority == "Low"    }) result += Success
    return result.take(3)
}

private fun buildWeekDays(
    assignments     : List<AssignmentDto>,
    selectedDateStr : String,
    anchorCal       : Calendar
): List<CalendarDay> {
    val todayStr = calStr(Calendar.getInstance())
    val dayNames = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")

    val monday = anchorCal.clone() as Calendar
    val dow    = monday.get(Calendar.DAY_OF_WEEK)
    val offset = if (dow == Calendar.SUNDAY) -6 else -(dow - Calendar.MONDAY)
    monday.add(Calendar.DAY_OF_YEAR, offset)

    return (0..6).map { i ->
        val c   = monday.clone() as Calendar
        c.add(Calendar.DAY_OF_YEAR, i)
        val iso = calStr(c)
        CalendarDay(
            dayLabel   = dayNames[i],
            date       = c.get(Calendar.DAY_OF_MONTH),
            isoDate    = iso,
            dots       = dotsForDay(iso, assignments),
            isSelected = iso == selectedDateStr,
            isToday    = iso == todayStr
        )
    }
}

private fun buildMonthGrid(
    assignments     : List<AssignmentDto>,
    selectedDateStr : String,
    displayCal      : Calendar
): List<CalendarDay> {
    val todayStr    = calStr(Calendar.getInstance())
    val dayNames    = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")

    val firstOfMonth = displayCal.clone() as Calendar
    firstOfMonth.set(Calendar.DAY_OF_MONTH, 1)
    val totalDays     = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDow      = firstOfMonth.get(Calendar.DAY_OF_WEEK)
    val leadingBlanks = if (firstDow == Calendar.SUNDAY) 6 else firstDow - Calendar.MONDAY

    val grid = mutableListOf<CalendarDay>()

    // Leading days from previous month
    val prevMonth = firstOfMonth.clone() as Calendar
    prevMonth.add(Calendar.DAY_OF_MONTH, -1)
    val prevTotal = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in leadingBlanks downTo 1) {
        val d = prevTotal - i + 1
        val c = prevMonth.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, d)
        val iso = calStr(c)
        grid += CalendarDay(
            dayLabel    = dayNames[grid.size % 7],
            date        = d,
            isoDate     = iso,
            dots        = dotsForDay(iso, assignments),
            isSelected  = iso == selectedDateStr,
            isToday     = iso == todayStr,
            isCurrentMo = false
        )
    }

    // Current month
    for (d in 1..totalDays) {
        val c = firstOfMonth.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, d)
        val iso = calStr(c)
        grid += CalendarDay(
            dayLabel    = dayNames[grid.size % 7],
            date        = d,
            isoDate     = iso,
            dots        = dotsForDay(iso, assignments),
            isSelected  = iso == selectedDateStr,
            isToday     = iso == todayStr,
            isCurrentMo = true
        )
    }

    // Trailing days to fill 6 rows × 7 cols = 42 cells
    val trailing  = 42 - grid.size
    val nextMonth = firstOfMonth.clone() as Calendar
    nextMonth.add(Calendar.MONTH, 1)
    for (d in 1..trailing) {
        val c = nextMonth.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, d)
        val iso = calStr(c)
        grid += CalendarDay(
            dayLabel    = dayNames[grid.size % 7],
            date        = d,
            isoDate     = iso,
            dots        = dotsForDay(iso, assignments),
            isSelected  = iso == selectedDateStr,
            isToday     = iso == todayStr,
            isCurrentMo = false
        )
    }

    return grid
}

private fun friendlyDate(isoDate: String, todayStr: String): String {
    val tomorrowCal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, 1) }
    val tomorrowStr = calStr(tomorrowCal)
    return when (isoDate) {
        todayStr    -> "Today"
        tomorrowStr -> "Tomorrow"
        else        -> {
            val parts = isoDate.split("-")
            if (parts.size == 3) {
                val months = listOf("Jan","Feb","Mar","Apr","May","Jun",
                    "Jul","Aug","Sep","Oct","Nov","Dec")
                val m = (parts[1].toIntOrNull() ?: 1) - 1
                val d = parts[2].toIntOrNull() ?: 0
                "${months.getOrElse(m) { "" }} $d"
            } else isoDate
        }
    }
}

// ── AssignmentDto → DeadlineItem ──────────────────────────────────────────────
private fun AssignmentDto.toDeadlineItem(): DeadlineItem {
    val dueDatePart = dueDate.take(10)

    val todayCal    = Calendar.getInstance()
    val tomorrowCal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, 1) }
    val today       = calStr(todayCal)
    val tomorrow    = calStr(tomorrowCal)

    val deadlineStatus = when {
        status == "overdue"     -> DeadlineStatus.OVERDUE
        dueDatePart == today    -> DeadlineStatus.TODAY
        dueDatePart == tomorrow -> DeadlineStatus.TOMORROW
        dueDatePart > today     -> DeadlineStatus.UPCOMING
        else                    -> DeadlineStatus.NEUTRAL
    }

    val accentColor = when {
        deadlineStatus == DeadlineStatus.OVERDUE   -> Purple
        deadlineStatus == DeadlineStatus.TODAY     -> Urgent
        deadlineStatus == DeadlineStatus.TOMORROW  -> Secondary
        priority == "High"                         -> Urgent
        priority == "Medium"                       -> Secondary
        else                                       -> Success
    }

    val icon = when {
        subject.contains("Math",     ignoreCase = true) -> Icons.Rounded.Calculate
        subject.contains("Web",      ignoreCase = true) -> Icons.Rounded.Code
        subject.contains("Database", ignoreCase = true) -> Icons.Rounded.Storage
        subject.contains("History",  ignoreCase = true) -> Icons.Rounded.HistoryEdu
        subject.contains("Science",  ignoreCase = true) -> Icons.Rounded.Science
        subject.contains("Physics",  ignoreCase = true) -> Icons.Rounded.Science
        subject.contains("English",  ignoreCase = true) -> Icons.Rounded.MenuBook
        else                                             -> Icons.Rounded.Assignment
    }

    val timePart = if (dueDate.length >= 16) {
        val rawTime = dueDate.substring(11, 16)
        val parts   = rawTime.split(":")
        val h       = parts[0].toIntOrNull() ?: 0
        val m       = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val ap      = if (h >= 12) "PM" else "AM"
        val h12     = if (h % 12 == 0) 12 else h % 12
        "%d:%02d %s".format(h12, m, ap)
    } else ""

    val dateLabel = when (deadlineStatus) {
        DeadlineStatus.TODAY    -> "Due Today"
        DeadlineStatus.TOMORROW -> "Tomorrow"
        DeadlineStatus.OVERDUE  -> "Overdue"
        DeadlineStatus.UPCOMING -> dueDatePart
        DeadlineStatus.NEUTRAL  -> dueDatePart
    }

    return DeadlineItem(
        id          = id,
        subject     = subject,
        description = description.ifBlank { title },
        status      = deadlineStatus,
        timeLabel   = timePart,
        dateLabel   = dateLabel,
        accentColor = accentColor,
        icon        = icon,
        tags        = tags,
        progress    = if (progress > 0) progress / 100f else -1f,
        priority    = priority,
        isoDate     = dueDatePart
    )
}

// ── Screen ─────────────────────────────────────────────────────────────────────
@Composable
fun DeadlineScreen(
    isDarkTheme              : Boolean             = false,
    viewModel                : AssignmentViewModel = AssignmentViewModel(),
    onBack                   : () -> Unit          = {},
    onNavigateToTasks        : () -> Unit          = {},
    onNavigateToAddAssignment: () -> Unit          = {},
    onNavigateToNotification : () -> Unit          = {},
    onNavigateToProfile      : () -> Unit          = {}
) {
    val bg  = if (isDarkTheme) BgDark  else BgLight
    val srf = if (isDarkTheme) SrfDark else SrfLight
    val txt = if (isDarkTheme) TxtDark else TxtLight

    val todayStr = remember { calStr(Calendar.getInstance()) }

    // ── Calendar state ─────────────────────────────────────────────────────────
    var viewMode         by remember { mutableStateOf(CalendarViewMode.WEEK) }
    var selectedDateStr  by remember { mutableStateOf(todayStr) }
    var selectedPriority by remember { mutableStateOf("All") }

    // Anchor drives prev/next navigation; always stays in sync with selected date's month/week
    var anchorCal by remember { mutableStateOf(Calendar.getInstance()) }

    // ── Data ───────────────────────────────────────────────────────────────────
    val assignmentsState by viewModel.assignments.collectAsState()

    val allAssignments: List<AssignmentDto> = when (val s = assignmentsState) {
        is UiState.Success -> s.data
        else               -> emptyList()
    }

    // ── Calendar grids ─────────────────────────────────────────────────────────
    val weekDays = remember(allAssignments, selectedDateStr, anchorCal.timeInMillis) {
        buildWeekDays(allAssignments, selectedDateStr, anchorCal)
    }
    val monthGrid = remember(allAssignments, selectedDateStr, anchorCal.timeInMillis) {
        buildMonthGrid(allAssignments, selectedDateStr, anchorCal)
    }

    // ── Assignment lists ───────────────────────────────────────────────────────
    val allDeadlineItems: List<DeadlineItem> = remember(allAssignments) {
        allAssignments
            .filter { it.status != "completed" }
            .sortedBy { it.dueDate }
            .map { it.toDeadlineItem() }
    }

    val deadlineItems: List<DeadlineItem> = remember(allDeadlineItems, selectedDateStr, selectedPriority) {
        allDeadlineItems
            .filter { it.isoDate == selectedDateStr }
            .filter { selectedPriority == "All" || it.priority == selectedPriority }
    }

    val totalDueForMonth: Int = remember(allDeadlineItems, anchorCal.timeInMillis) {
        val prefix = "%04d-%02d".format(
            anchorCal.get(Calendar.YEAR),
            anchorCal.get(Calendar.MONTH) + 1
        )
        allDeadlineItems.count { it.isoDate.startsWith(prefix) }
    }

    val assignmentCountByDate: Map<String, Int> = remember(allDeadlineItems) {
        allDeadlineItems.groupBy { it.isoDate }.mapValues { it.value.size }
    }

    val filterLabel = friendlyDate(selectedDateStr, todayStr)

    // ── Nav helpers ────────────────────────────────────────────────────────────
    fun navigatePrev() {
        val c = anchorCal.clone() as Calendar
        if (viewMode == CalendarViewMode.MONTH) c.add(Calendar.MONTH, -1)
        else c.add(Calendar.WEEK_OF_YEAR, -1)
        anchorCal = c
    }

    fun navigateNext() {
        val c = anchorCal.clone() as Calendar
        if (viewMode == CalendarViewMode.MONTH) c.add(Calendar.MONTH, 1)
        else c.add(Calendar.WEEK_OF_YEAR, 1)
        anchorCal = c
    }

    fun jumpToToday() {
        anchorCal       = Calendar.getInstance()
        selectedDateStr = todayStr
    }

    AsTrackerTheme(darkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header ─────────────────────────────────────────────────────
                DeadlineHeader(
                    isDark               = isDarkTheme,
                    srf                  = srf,
                    txt                  = txt,
                    totalDue             = deadlineItems.size,
                    totalDueForMonth     = totalDueForMonth,
                    monthYearLabel       = monthYearOf(anchorCal),
                    weekDays             = weekDays,
                    monthGrid            = monthGrid,
                    viewMode             = viewMode,
                    onViewModeChange     = { viewMode = it },
                    onDaySelected        = { iso ->
                        selectedDateStr = iso
                        val parts = iso.split("-")
                        if (parts.size == 3) {
                            val c = anchorCal.clone() as Calendar
                            c.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                            anchorCal = c
                        }
                    },
                    onPrev               = ::navigatePrev,
                    onNext               = ::navigateNext,
                    onTodayClick         = ::jumpToToday,
                    onBack               = onBack,
                    selectedDateStr      = selectedDateStr,
                    todayStr             = todayStr,
                    assignmentCountByDate = assignmentCountByDate
                )

                // ── Scrollable body ────────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp,
                        top = 20.dp, bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {

                    // ── Section header ─────────────────────────────────────────
                    item {
                        SectionHeader(
                            filterLabel     = filterLabel,
                            selectedDateStr = selectedDateStr,
                            todayStr        = todayStr,
                            count           = deadlineItems.size,
                            isDark          = isDarkTheme,
                            txt             = txt
                        )
                    }

                    // ── Priority filter chips ──────────────────────────────────
                    item {
                        PriorityFilterRow(
                            selected = selectedPriority,
                            onSelect = { selectedPriority = it },
                            isDark   = isDarkTheme,
                            srf      = srf
                        )
                    }

                    // ── Loading / Error / Content ──────────────────────────────
                    when (val s = assignmentsState) {
                        is UiState.Loading -> item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color    = PrimaryColor,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        "Loading assignments…",
                                        color    = TxtMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        is UiState.Error -> item {
                            ErrorCard(message = s.message, isDark = isDarkTheme, srf = srf)
                        }

                        is UiState.Success -> {
                            if (deadlineItems.isEmpty()) {
                                item {
                                    EmptyDayCard(
                                        filterLabel      = filterLabel,
                                        selectedPriority = selectedPriority,
                                        isDark           = isDarkTheme,
                                        srf              = srf,
                                        onAddClick       = onNavigateToAddAssignment
                                    )
                                }
                            } else {
                                items(
                                    items = deadlineItems,
                                    key   = { it.id.ifBlank { it.isoDate + it.subject } }
                                ) { item ->
                                    TimelineItem(
                                        item   = item,
                                        isDark = isDarkTheme,
                                        srf    = srf,
                                        txt    = txt,
                                        isLast = item == deadlineItems.last()
                                    )
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }

            // ── FAB ────────────────────────────────────────────────────────────
            FloatingActionButton(
                onClick        = onNavigateToAddAssignment,
                containerColor = PrimaryColor,
                contentColor   = Color.White,
                shape          = CircleShape,
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 88.dp)
                    .size(56.dp)
                    .shadow(12.dp, CircleShape, spotColor = PrimaryColor.copy(0.4f))
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
            }

            // ── Bottom nav ─────────────────────────────────────────────────────
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

// ── Header ─────────────────────────────────────────────────────────────────────
@Composable
private fun DeadlineHeader(
    isDark               : Boolean,
    srf                  : Color,
    txt                  : Color,
    totalDue             : Int,
    totalDueForMonth     : Int,
    monthYearLabel       : String,
    weekDays             : List<CalendarDay>,
    monthGrid            : List<CalendarDay>,
    viewMode             : CalendarViewMode,
    onViewModeChange     : (CalendarViewMode) -> Unit,
    onDaySelected        : (String) -> Unit,
    onPrev               : () -> Unit,
    onNext               : () -> Unit,
    onTodayClick         : () -> Unit,
    onBack               : () -> Unit,
    selectedDateStr      : String,
    todayStr             : String,
    assignmentCountByDate: Map<String, Int>
) {
    Surface(
        color           = srf,
        shadowElevation = 6.dp,
        shape           = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier        = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 16.dp)
        ) {

            // ── Top bar ────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = "Back",
                        tint     = TxtMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    "Deadline Tracker",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color      = txt
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDark) Color(0xFF312E81).copy(0.35f) else Color(0xFFE0E7FF)
                        )
                        .clickable(onClick = onTodayClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Today",
                        color      = PrimaryColor,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── Month/Year + prev/next + toggle ────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            contentDescription = "Previous",
                            tint     = TxtMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    AnimatedContent(
                        targetState = monthYearLabel,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                        label = "month_label"
                    ) { label ->
                        Column {
                            Text(
                                label,
                                fontSize   = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color      = txt
                            )
                            val sub = when (viewMode) {
                                CalendarViewMode.MONTH ->
                                    "$totalDueForMonth due this month"
                                CalendarViewMode.WEEK ->
                                    "$totalDue due on selected day"
                            }
                            Text(
                                sub,
                                fontSize   = 11.sp,
                                color      = TxtMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(Modifier.width(2.dp))
                    IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = "Next",
                            tint     = TxtMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Week / Month toggle pill
                ViewModeToggle(
                    current  = viewMode,
                    onChange = onViewModeChange,
                    isDark   = isDark,
                    srf      = srf
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Day-of-week header row ─────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach { d ->
                    val isWeekend = d == "Sat" || d == "Sun"
                    Text(
                        text       = d,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.Center,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isWeekend)
                            (if (isDark) Color(0xFF6B7280) else Color(0xFFD1D5DB))
                        else
                            TxtMuted
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Calendar body (animated Week ↔ Month) ─────────────────────────
            AnimatedContent(
                targetState = viewMode,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "calendar_body"
            ) { mode ->
                when (mode) {
                    CalendarViewMode.WEEK -> WeekStrip(
                        days      = weekDays,
                        isDark    = isDark,
                        onClick   = onDaySelected
                    )
                    CalendarViewMode.MONTH -> MonthGrid(
                        grid      = monthGrid,
                        isDark    = isDark,
                        onClick   = onDaySelected
                    )
                }
            }
        }
    }
}

// ── Week / Month toggle ────────────────────────────────────────────────────────
@Composable
private fun ViewModeToggle(
    current  : CalendarViewMode,
    onChange : (CalendarViewMode) -> Unit,
    isDark   : Boolean,
    srf      : Color
) {
    Row(
        modifier = Modifier
            .background(
                if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6),
                RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        listOf(CalendarViewMode.WEEK to "Week", CalendarViewMode.MONTH to "Month").forEach { (mode, label) ->
            val isActive = current == mode
            val bg by animateColorAsState(
                if (isActive) srf else Color.Transparent,
                tween(200), label = "toggle_$label"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg)
                    .clickable { onChange(mode) }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize   = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isActive) PrimaryColor else TxtMuted
                )
            }
        }
    }
}

// ── Week strip ─────────────────────────────────────────────────────────────────
@Composable
private fun WeekStrip(
    days    : List<CalendarDay>,
    isDark  : Boolean,
    onClick : (String) -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            DayCell(
                day     = day,
                isDark  = isDark,
                compact = false,
                onClick = { onClick(day.isoDate) }
            )
        }
    }
}

// ── Month grid ─────────────────────────────────────────────────────────────────
@Composable
private fun MonthGrid(
    grid    : List<CalendarDay>,
    isDark  : Boolean,
    onClick : (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        grid.chunked(7).forEach { week ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { day ->
                    DayCell(
                        day     = day,
                        isDark  = isDark,
                        compact = true,
                        onClick = { onClick(day.isoDate) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Pad last row if shorter than 7
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ── Day cell ───────────────────────────────────────────────────────────────────
@Composable
private fun DayCell(
    day      : CalendarDay,
    isDark   : Boolean,
    compact  : Boolean = false,
    onClick  : () -> Unit = {},
    modifier : Modifier = Modifier
) {
    val cellSize  = if (compact) 34.dp else 40.dp
    val fontSize  = if (compact) 13.sp else 15.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp, horizontal = 1.dp)
    ) {
        if (!compact) {
            Text(
                text       = day.dayLabel,
                fontSize   = 11.sp,
                fontWeight = if (day.isSelected) FontWeight.Bold else FontWeight.Medium,
                color      = when {
                    day.isSelected -> PrimaryColor
                    isDark         -> Color(0xFF9CA3AF)
                    else           -> TxtMuted
                }
            )
            Spacer(Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .size(cellSize)
                .then(
                    when {
                        day.isSelected -> Modifier
                            .shadow(6.dp, CircleShape, spotColor = PrimaryColor.copy(0.3f))
                            .background(PrimaryColor, CircleShape)
                        day.isToday && !day.isSelected -> Modifier
                            .border(1.5.dp, PrimaryColor.copy(0.6f), CircleShape)
                        else -> Modifier.background(Color.Transparent, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = day.date.toString(),
                fontSize   = fontSize,
                fontWeight = if (day.isSelected || day.isToday) FontWeight.Bold else FontWeight.SemiBold,
                color      = when {
                    day.isSelected  -> Color.White
                    !day.isCurrentMo -> TxtMuted.copy(alpha = 0.38f)
                    day.isToday     -> PrimaryColor
                    isDark          -> Color(0xFFD1D5DB)
                    else            -> Color(0xFF4B5563)
                }
            )
        }

        Spacer(Modifier.height(3.dp))

        // Priority dots row
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (day.dots.isEmpty()) {
                Box(modifier = Modifier.size(6.dp))   // placeholder to keep height stable
            } else {
                day.dots.forEach { dotColor ->
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(
                                dotColor.copy(alpha = if (day.isCurrentMo) 1f else 0.4f),
                                CircleShape
                            )
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

// ── Section header ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(
    filterLabel     : String,
    selectedDateStr : String,
    todayStr        : String,
    count           : Int,
    isDark          : Boolean,
    txt             : Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text       = "Deadlines · $filterLabel",
                fontSize   = 17.sp,
                fontWeight = FontWeight.Bold,
                color      = txt
            )
            if (selectedDateStr == todayStr) {
                Text(
                    text     = "Keep track of what's due today",
                    fontSize = 11.sp,
                    color    = TxtMuted
                )
            }
        }
        if (count > 0) {
            Box(
                modifier = Modifier
                    .background(
                        if (isDark) Color(0xFF312E81).copy(0.4f) else Color(0xFFE0E7FF),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = "$count",
                    color      = PrimaryColor,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Priority filter chips ──────────────────────────────────────────────────────
@Composable
private fun PriorityFilterRow(
    selected : String,
    onSelect : (String) -> Unit,
    isDark   : Boolean,
    srf      : Color
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier              = Modifier.padding(bottom = 14.dp)
    ) {
        items(listOf("All", "High", "Medium", "Low")) { p ->
            val isActive   = p == selected
            val chipColor  = when {
                isActive && p == "High"   -> Urgent
                isActive && p == "Medium" -> Secondary
                isActive && p == "Low"    -> Success
                isActive                  -> PrimaryColor
                else                      -> Color.Transparent
            }
            val dotColor = when (p) {
                "High"   -> Urgent
                "Medium" -> Secondary
                "Low"    -> Success
                else     -> Color.Transparent
            }
            val textColor = if (isActive) Color.White
                else if (isDark) Color(0xFFD1D5DB)
                else Color(0xFF4B5563)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isActive) chipColor
                        else if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                    )
                    .clickable { onSelect(p) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (p != "All") {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (isActive) Color.White.copy(0.8f) else dotColor,
                                CircleShape
                            )
                    )
                }
                Text(
                    text       = p,
                    color      = textColor,
                    fontSize   = 13.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ── Timeline item ──────────────────────────────────────────────────────────────
@Composable
private fun TimelineItem(
    item   : DeadlineItem,
    isDark : Boolean,
    srf    : Color,
    txt    : Color,
    isLast : Boolean
) {
    val dimmed    = item.status == DeadlineStatus.NEUTRAL
    val cardAlpha = if (dimmed) 0.65f else 1f

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Timeline spine ─────────────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape    = CircleShape,
                color    = item.accentColor.copy(alpha = 0.12f),
                border   = androidx.compose.foundation.BorderStroke(2.dp, item.accentColor),
                modifier = Modifier.size(40.dp)
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
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(
                            if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                        )
                )
            }
        }

        // ── Card ───────────────────────────────────────────────────────────────
        Card(
            shape     = RoundedCornerShape(14.dp),
            colors    = CardDefaults.cardColors(containerColor = srf.copy(alpha = cardAlpha)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier  = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 14.dp)
        ) {
            // Coloured left accent bar
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            item.accentColor.copy(alpha = cardAlpha),
                            RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                        )
                )
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {

                    // Status row
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            StatusBadge(item = item, isDark = isDark)
                            PriorityBadge(priority = item.priority, alpha = cardAlpha)
                        }
                        if (item.timeLabel.isNotEmpty()) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.AccessTime,
                                    contentDescription = null,
                                    tint     = TxtMuted,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    item.timeLabel,
                                    fontSize   = 11.sp,
                                    color      = TxtMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        item.subject,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = txt.copy(alpha = cardAlpha)
                    )
                    Text(
                        item.description,
                        fontSize = 12.sp,
                        color    = TxtMuted.copy(alpha = cardAlpha),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 3.dp)
                    )

                    // Progress bar
                    if (item.progress >= 0f) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress   = { item.progress },
                                modifier   = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                                color      = item.accentColor,
                                trackColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                            )
                            Text(
                                "${(item.progress * 100).toInt()}%",
                                fontSize   = 10.sp,
                                color      = item.accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Tags
                    if (item.tags.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item.tags.take(3).forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        tag,
                                        fontSize = 10.sp,
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
}

// ── Status badge ───────────────────────────────────────────────────────────────
@Composable
private fun StatusBadge(item: DeadlineItem, isDark: Boolean) {
    val bg = when (item.status) {
        DeadlineStatus.TODAY    -> if (isDark) Color(0xFF7F1D1D).copy(0.4f) else Color(0xFFFEF2F2)
        DeadlineStatus.TOMORROW -> if (isDark) Color(0xFF7C2D12).copy(0.4f) else Color(0xFFFFF7ED)
        DeadlineStatus.UPCOMING -> if (isDark) Color(0xFF064E3B).copy(0.4f) else Color(0xFFECFDF5)
        DeadlineStatus.OVERDUE  -> if (isDark) Color(0xFF3B0764).copy(0.4f) else Color(0xFFF3E8FF)
        DeadlineStatus.NEUTRAL  -> if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            item.dateLabel.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            color         = item.accentColor,
            letterSpacing = 0.8.sp
        )
    }
}

// ── Priority badge ─────────────────────────────────────────────────────────────
@Composable
private fun PriorityBadge(priority: String, alpha: Float = 1f) {
    val (bg, tint) = when (priority) {
        "High"   -> Color(0xFFFEF2F2) to Urgent
        "Medium" -> Color(0xFFFFF7ED) to Secondary
        else     -> Color(0xFFECFDF5) to Success
    }
    Box(
        modifier = Modifier
            .background(bg.copy(alpha = alpha), RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            priority.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            color         = tint.copy(alpha = alpha),
            letterSpacing = 0.8.sp
        )
    }
}

// ── Empty day card ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyDayCard(
    filterLabel      : String,
    selectedPriority : String,
    isDark           : Boolean,
    srf              : Color,
    onAddClick       : () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = srf),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.EventAvailable,
                    contentDescription = null,
                    tint     = TxtMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                "No assignments on $filterLabel",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = if (isDark) TxtDark else TxtLight
            )
            if (selectedPriority != "All") {
                Text(
                    "Priority filter: $selectedPriority",
                    fontSize = 12.sp,
                    color    = TxtMuted
                )
            }
            Text(
                "Enjoy the free time — or add a new assignment!",
                fontSize  = 12.sp,
                color     = TxtMuted,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PrimaryColor)
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 20.dp, vertical = 9.dp)
            ) {
                Text(
                    "+ Add Assignment",
                    color      = Color.White,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Error card ─────────────────────────────────────────────────────────────────
@Composable
private fun ErrorCard(
    message : String,
    isDark  : Boolean,
    srf     : Color
) {
    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = srf),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint     = Urgent,
                modifier = Modifier.size(24.dp)
            )
            Text(
                message,
                color    = Urgent,
                fontSize = 13.sp
            )
        }
    }
}

// ── Bottom nav ─────────────────────────────────────────────────────────────────
@Composable
private fun DeadlineBottomNav(
    isDark                   : Boolean,
    srf                      : Color,
    onNavigateToTasks        : () -> Unit,
    onNavigateToNotification : () -> Unit = {},
    onNavigateToProfile      : () -> Unit = {},
    modifier                 : Modifier   = Modifier
) {
    Surface(
        color           = srf,
        shadowElevation = 16.dp,
        shape           = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier        = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            DeadlineNavItem(
                icon       = Icons.Rounded.Assignment,
                label      = "Tasks",
                isSelected = false,
                onClick    = onNavigateToTasks
            )
            DeadlineNavItem(
                icon       = Icons.Rounded.CalendarToday,
                label      = "Calendar",
                isSelected = true,
                onClick    = {}
            )
            DeadlineNavItem(
                icon       = Icons.Rounded.Notifications,
                label      = "Alerts",
                isSelected = false,
                hasBadge   = true,
                onClick    = onNavigateToNotification
            )
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
    icon       : ImageVector,
    label      : String,
    isSelected : Boolean,
    hasBadge   : Boolean  = false,
    onClick    : () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable(onClick = onClick)
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
                        .background(Urgent, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text       = label,
            fontSize   = 10.sp,
            color      = if (isSelected) PrimaryColor else TxtMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ── Previews ───────────────────────────────────────────────────────────────────
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
