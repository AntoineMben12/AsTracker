package com.example.astracker.ui.assignment

import androidx.compose.ui.draw.shadow
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.ui.common.UiState
import com.example.astracker.ui.assignment.AssignmentViewModel
import com.example.astracker.network.models.AssignmentDto
import com.example.astracker.ui.theme.AsTrackerTheme

// Custom Colors from HTML
val PrimaryColor = Color(0xFF6366f1) // Indigo 500
val SecondaryColor = Color(0xFF8b5cf6) // Violet 500
val BackgroundLight = Color(0xFFf3f4f6) // Gray 100
val BackgroundDark = Color(0xFF111827) // Gray 900
val SurfaceLight = Color(0xFFffffff)
val SurfaceDark = Color(0xFF1f2937) // Gray 800
val TextLight = Color(0xFF1f2937)
val TextDark = Color(0xFFf9fafb)

@Composable
fun AssignmentListScreen(
    viewModel: AssignmentViewModel,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAddAssignment: () -> Unit = {},
    onNavigateToDeadline: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {}
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    val backgroundColor = if (isDarkTheme) BackgroundDark else BackgroundLight
    val textPrimary = if (isDarkTheme) TextDark else TextLight

    val assignmentsState by viewModel.assignments.collectAsState()

    AsTrackerTheme(darkTheme = isDarkTheme) {
        Scaffold(
            containerColor = backgroundColor,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToAddAssignment,
                    containerColor = if (isDarkTheme) Color.White else Color(0xFF111827),
                    contentColor = if (isDarkTheme) Color.Black else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                }
            },
            bottomBar = {
                BottomNavigationBar(
                    isDarkTheme,
                    onNavigateToDeadline = onNavigateToDeadline,
                    onNavigateToNotification = onNavigateToNotification,
                    onNavigateToProfile = onNavigateToProfile
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                HeaderSection(isDarkTheme, onThemeToggle = { isDarkTheme = !isDarkTheme })
                Spacer(modifier = Modifier.height(24.dp))
                SearchBar(isDarkTheme)
                Spacer(modifier = Modifier.height(24.dp))
                FilterSection(isDarkTheme)
                Spacer(modifier = Modifier.height(24.dp))

                when (val state = assignmentsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryColor)
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                    is UiState.Success -> {
                        val list = state.data
                        val today = java.time.LocalDate.now().toString()
                        val dueToday = list.filter { it.dueDate.startsWith(today) }
                        val upcoming = list.filter { !it.dueDate.startsWith(today) }

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            if (dueToday.isNotEmpty()) {
                                item {
                                    Text(
                                        "Due Today",
                                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                items(dueToday) { assignment ->
                                    AssignmentCard(assignment, isDarkTheme,
                                        onComplete = { viewModel.markComplete(assignment._id) },
                                        onDelete = { viewModel.deleteAssignment(assignment._id) }
                                    )
                                }
                            }

                            item {
                                Text(
                                    "Upcoming",
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                                )
                            }
                            if (upcoming.isEmpty()) {
                                item {
                                    Text(
                                        "No upcoming assignments 🎉",
                                        color = Color.Gray, fontSize = 14.sp,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            } else {
                                items(upcoming) { assignment ->
                                    AssignmentCard(assignment, isDarkTheme,
                                        onComplete = { viewModel.markComplete(assignment._id) },
                                        onDelete = { viewModel.deleteAssignment(assignment._id) }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// --- Components ---

@Composable
fun HeaderSection(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "My Assignments",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) TextDark else TextLight
            )
            Text(
                text = "You have 4 tasks due soon.",
                fontSize = 14.sp,
                color = if (isDarkTheme) Color.Gray else Color.Gray
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isDarkTheme) Color(0xFF1f2937) else Color(0xFFf3f4f6),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = "Theme Toggle",
                    tint = if (isDarkTheme) Color.Gray else Color.Gray
                )
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PrimaryColor, SecondaryColor)))
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) SurfaceDark else SurfaceLight)
            ) {
                // Placeholder for Image
                 Box(
                     modifier = Modifier
                         .fillMaxSize()
                         .background(Color.Gray.copy(alpha = 0.3f)),
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(Icons.Rounded.Person, contentDescription = "Profile", tint = Color.Gray)
                 }
                 // In real app use Coil/Glide:
                 // AsyncImage(model = "url", contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
            }
        }
    }
}

@Composable
fun SearchBar(isDarkTheme: Boolean) {
    val backgroundColor = if (isDarkTheme) SurfaceDark else SurfaceLight
    val contentColor = if (isDarkTheme) TextDark else TextLight
    
    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search tasks, subjects...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (isDarkTheme) Color(0xFF374151) else Color(0xFFf3f4f6), RoundedCornerShape(8.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = "Filter", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,
                disabledContainerColor = backgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PrimaryColor,
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.1f))
        )
    }
}

@Composable
fun FilterSection(isDarkTheme: Boolean) {
    val filters = listOf("All", "Pending", "Completed", "Overdue")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(filters) { filter ->
            val isSelected = filter == "All"
            val backgroundColor = if (isSelected) PrimaryColor else if (isDarkTheme) SurfaceDark else SurfaceLight
            val textColor = if (isSelected) Color.White else if (isDarkTheme) Color(0xFFd1d5db) else Color(0xFF4b5563)
            val border = if (isSelected) null else if (isDarkTheme)  androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFe5e7eb))
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .then(if (border != null) Modifier.border(border, RoundedCornerShape(50)) else Modifier)
                    .background(backgroundColor)
                    .clickable { }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = filter,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DueTodaySection(isDarkTheme: Boolean) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "Due Today",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) TextDark else TextLight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier
                .size(8.dp)
                .background(Color.Red, CircleShape))
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Border Left Simulate
                // Note: Compose doesn't have a simple left-border, so we can wrap content or just skip for simplicity 
                // Using a Row with a thin box for the red line
                
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.Top
                ) {
                    
                    Box(modifier = Modifier
                        .background(Color(0xFFfee2e2), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "HIGH PRIORITY",
                            color = Color(0xFFdc2626),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = "Expand",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Calculus II: Integration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) TextDark else TextLight
                )
                
                Text(
                    text = "Chapter 4 exercises on definite integrals and area under curves.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = if (isDarkTheme) Color(0xFF374151) else Color(0xFFf3f4f6)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "11:59 PM", color = Color(0xFFef4444), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                    
                    // Avatar Group Placeholder
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFdbeafe), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("M", color = Color(0xFF2563eb), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Tasks
                TaskItem(isDarkTheme, "Read Chapter 4", true)
                TaskItem(isDarkTheme, "Complete Practice Problems", true)
                TaskItem(isDarkTheme, "Review Notes", true)
                TaskItem(isDarkTheme, "Prepare Final Summary", false)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                     Text("3/4 tasks completed", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                     Text("75% done", fontSize = 12.sp, color = PrimaryColor, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Assignment", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun TaskItem(isDarkTheme: Boolean, text: String, isChecked: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        val checkboxColor = if (isChecked) PrimaryColor else Color.Gray
        Icon(
            imageVector = if (isChecked) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = checkboxColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isChecked) Color.Gray else if (isDarkTheme) Color.Gray else Color(0xFF374151),
            textDecoration = if (isChecked) TextDecoration.LineThrough else null
        )
    }
}

@Composable
fun UpcomingTaskCard(task: UpcomingTask, isDarkTheme: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Colored Bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(task.color)
            )
            
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(task.color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(task.subject, color = task.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• ${task.type}", color = Color.Gray, fontSize = 10.sp)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) TextDark else TextLight
                )
                
                 Spacer(modifier = Modifier.height(8.dp))
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(task.date, fontSize = 12.sp, color = Color.Gray)
                 }
            }
            
            Box(
                modifier = Modifier.padding(16.dp).align(Alignment.Top).size(32.dp).background(if (isDarkTheme) Color(0xFF374151) else Color(0xFFf9fafb), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    isDarkTheme: Boolean,
    onNavigateToDeadline: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val backgroundColor = if (isDarkTheme) SurfaceDark else SurfaceLight
    
    Surface(
        color = backgroundColor,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(icon = Icons.Rounded.Assignment,    label = "Tasks",    isSelected = true)
            NavBarItem(icon = Icons.Rounded.CalendarToday, label = "Calendar", isSelected = false, onClick = onNavigateToDeadline)
            NavBarItem(icon = Icons.Rounded.Notifications, label = "Alerts",   isSelected = false, hasBadge = true, onClick = onNavigateToNotification)
            NavBarItem(icon = Icons.Rounded.Person,        label = "Profile",  isSelected = false, onClick = onNavigateToProfile)
        }
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    hasBadge: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box {
             Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) PrimaryColor else Color.Gray,
                modifier = Modifier.size(24.dp)
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
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) PrimaryColor else Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

// Reusable Modifier for shadow is complex in Compose without external libs unless using elevation
// but M3 cards have elevation.

// Helper modifier for standard shadow
 // Ensure this import is at the top

fun Modifier.customShadow(
    elevation: androidx.compose.ui.unit.Dp,
    shape: androidx.compose.ui.graphics.Shape,
    spotColor: Color
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    spotColor = spotColor
)
// ── API assignment card ────────────────────────────────────────────────────────
@Composable
fun AssignmentCard(assignment: AssignmentDto, isDarkTheme: Boolean, onComplete: () -> Unit, onDelete: () -> Unit) {
    val priorityColor = when (assignment.priority) {
        "High" -> Color(0xFFEF4444)
        "Medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(priorityColor))
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(priorityColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(assignment.subject, color = priorityColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("• ${assignment.type}", color = Color.Gray, fontSize = 10.sp)
                    Spacer(Modifier.weight(1f))
                    Text(
                        assignment.status.uppercase(),
                        color = when (assignment.status) {
                            "completed" -> Color(0xFF10B981)
                            "overdue" -> Color(0xFFEF4444)
                            else -> Color.Gray
                        },
                        fontSize = 9.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(assignment.title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) TextDark else TextLight)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { assignment.progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = priorityColor
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(assignment.dueDate.take(10), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                    if (assignment.status != "completed") {
                        TextButton(onClick = onComplete, contentPadding = PaddingValues(0.dp)) {
                            Text("Mark Done", fontSize = 11.sp, color = Color(0xFF10B981))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ── Helpers kept for bottom nav / static UI ────────────────────────────────────
class SystemUiControllerFake

// Preview stub
// @Preview
// fun AssignmentListScreenPreview() { AssignmentListScreen(viewModel = fakeVM) }
