package com.example.astracker.ui.assignment

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.ui.theme.AsTrackerTheme

// ── Colour tokens (reuse AssignmentListScreen palette) ────────────────────────
private val Primary      = Color(0xFF5B50E5)
private val BgLight      = Color(0xFFF2F4F8)
private val CardLight    = Color(0xFFFFFFFF)
private val BgDark       = Color(0xFF121212)
private val CardDark     = Color(0xFF1E1E1E)
private val TxtLight     = Color(0xFF111827)
private val TxtDark      = Color(0xFFF3F4F6)
private val TxtMuted     = Color(0xFF6B7280)
private val MediumBorder = Color(0xFFEAB308)
private val MediumBg     = Color(0xFFFEFCE8)
private val GlowShadow   = Color(0x4D5B50E5)

// ── Data ──────────────────────────────────────────────────────────────────────
private data class Subtask(val title: String, var checked: Boolean)

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun AddAssignmentScreen(
    isDarkTheme: Boolean = false,
    onBack: () -> Unit = {}
) {
    val bg    = if (isDarkTheme) BgDark   else BgLight
    val card  = if (isDarkTheme) CardDark else CardLight
    val txt   = if (isDarkTheme) TxtDark  else TxtLight

    // ── Form state ────────────────────────────────────────────────────────────
    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate     by remember { mutableStateOf("") }
    var priority    by remember { mutableStateOf("Medium") }   // Low | Medium | High
    var subtaskInput by remember { mutableStateOf("") }
    val subtasks = remember {
        mutableStateListOf(
            Subtask("Read Chapter 1",      true),
            Subtask("Summarize Key Points", false),
            Subtask("Review Examples",      false)
        )
    }

    AsTrackerTheme(darkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
        ) {
            // ── Scrollable content ─────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 80.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ── Assignment Title ──────────────────────────────────────────
                item {
                    FormSection(label = "Assignment Title") {
                        FormTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "e.g. Chapter 5 Summary",
                            card = card, txt = txt, isDark = isDarkTheme
                        )
                    }
                }

                // ── Description ───────────────────────────────────────────────
                item {
                    FormSection(label = "Assignment Description") {
                        FormTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Enter assignment details here...",
                            card = card, txt = txt, isDark = isDarkTheme,
                            minLines = 4, maxLines = 6
                        )
                    }
                }

                // ── Subject ───────────────────────────────────────────────────
                item {
                    FormSection(label = "Subject") {
                        SubjectDropdown(card = card, txt = txt, isDark = isDarkTheme)
                    }
                }

                // ── Due Date ──────────────────────────────────────────────────
                item {
                    FormSection(label = "Due Date") {
                        DueDateField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            card = card, txt = txt, isDark = isDarkTheme
                        )
                    }
                }

                // ── Priority ──────────────────────────────────────────────────
                item {
                    FormSection(label = "Priority") {
                        PrioritySelector(
                            selected = priority,
                            onSelect = { priority = it },
                            card = card, isDark = isDarkTheme
                        )
                    }
                }

                // ── Subtasks ──────────────────────────────────────────────────
                item {
                    FormSection(label = "Subtasks") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Input row
                            Box {
                                FormTextField(
                                    value = subtaskInput,
                                    onValueChange = { subtaskInput = it },
                                    placeholder = "Add a subtask...",
                                    card = card, txt = txt, isDark = isDarkTheme,
                                    endPadding = 52.dp
                                )
                                IconButton(
                                    onClick = {
                                        if (subtaskInput.isNotBlank()) {
                                            subtasks.add(Subtask(subtaskInput.trim(), false))
                                            subtaskInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 8.dp)
                                        .size(36.dp)
                                        .background(
                                            if (isDarkTheme) Color(0xFF374151) else Color(0xFFF3F4F6),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = "Add subtask",
                                        tint = TxtMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            // List
                            subtasks.forEachIndexed { index, subtask ->
                                SubtaskItem(
                                    subtask   = subtask,
                                    isDark    = isDarkTheme,
                                    onToggle  = { subtasks[index] = subtask.copy(checked = !subtask.checked) },
                                    onDelete  = { subtasks.removeAt(index) }
                                )
                            }
                        }
                    }
                }
            }

            // ── Top header ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .statusBarsPadding()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = txt,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "New Assignment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = txt,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // ── Save button (fixed bottom) ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(bg)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Button(
                    onClick = { /* TODO: persist assignment */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = GlowShadow
                        )
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Save Assignment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Small helpers ─────────────────────────────────────────────────────────────

@Composable
private fun FormSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TxtLight,
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    card: Color,
    txt: Color,
    isDark: Boolean,
    minLines: Int = 1,
    maxLines: Int = 1,
    endPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color.Black.copy(0.05f))
            .background(card, RoundedCornerShape(12.dp))
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = txt,
                fontSize = 15.sp
            ),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = endPadding, top = 14.dp, bottom = 14.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = TxtMuted, fontSize = 15.sp)
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun SubjectDropdown(card: Color, txt: Color, isDark: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val subjects  = listOf("Mathematics", "Physics", "History", "English", "Chemistry")
    var selected  by remember { mutableStateOf(subjects[0]) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color.Black.copy(0.05f))
            .background(card, RoundedCornerShape(12.dp))
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(Color.Gray.copy(0.2f), RoundedCornerShape(6.dp))
                    .padding(4.dp)
            ) {
                Icon(
                    Icons.Outlined.Book,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(selected, color = txt, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = TxtMuted
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            subjects.forEach { subj ->
                DropdownMenuItem(
                    text = { Text(subj) },
                    onClick = {
                        selected = subj
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DueDateField(
    value: String,
    onValueChange: (String) -> Unit,
    card: Color,
    txt: Color,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color.Black.copy(0.05f))
            .background(card, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = TxtMuted, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(color = txt, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                decorationBox = { inner ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) Text("mm/dd/yyyy", color = txt.copy(alpha = 0.5f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        inner()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Outlined.Event, contentDescription = null, tint = TxtMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PrioritySelector(selected: String, onSelect: (String) -> Unit, card: Color, isDark: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("Low", "Medium", "High").forEach { level ->
            val isSelected = selected == level
            val bgColor by animateColorAsState(
                targetValue = when {
                    isSelected && level == "Medium" -> MediumBg
                    isSelected && level == "High"   -> Color(0xFFFEF2F2)
                    isSelected && level == "Low"    -> Color(0xFFF0FDF4)
                    isDark                          -> Color(0xFF1E1E1E)
                    else                            -> card
                },
                label = "priority_bg"
            )
            val borderColor = when {
                isSelected && level == "Medium" -> MediumBorder
                isSelected && level == "High"   -> Color(0xFFEF4444)
                isSelected && level == "Low"    -> Color(0xFF22C55E)
                else                            -> Color.Transparent
            }
            val textColor = when {
                isSelected && level == "Medium" -> Color(0xFFCA8A04)
                isSelected && level == "High"   -> Color(0xFFDC2626)
                isSelected && level == "Low"    -> Color(0xFF16A34A)
                isDark                          -> TxtDark
                else                            -> TxtLight
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color.Black.copy(0.05f))
                    .background(bgColor, RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable { onSelect(level) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(level, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SubtaskItem(
    subtask: Subtask,
    isDark: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (subtask.checked) if (isDark) Color(0xFFE5E7EB) else Color(0xFF1F2937) else Color.Transparent)
                    .border(
                        width = if (subtask.checked) 0.dp else 2.dp,
                        color = if (subtask.checked) Color.Transparent else Color(0xFFD1D5DB),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (subtask.checked) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = if (isDark) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = subtask.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) TxtDark else TxtLight,
                textDecoration = if (subtask.checked) TextDecoration.LineThrough else null
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Outlined.DeleteOutline,
                contentDescription = "Delete subtask",
                tint = TxtMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Alias so Compose finds BasicTextField ─────────────────────────────────────
@Composable
private fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle.Default,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        @Composable { innerTextField -> innerTextField() }
) {
    androidx.compose.foundation.text.BasicTextField(
        value           = value,
        onValueChange   = onValueChange,
        modifier        = modifier,
        textStyle       = textStyle,
        minLines        = minLines,
        maxLines        = maxLines,
        decorationBox   = decorationBox
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun AddAssignmentScreenPreview() {
    AddAssignmentScreen()
}
