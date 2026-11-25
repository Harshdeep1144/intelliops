package com.harshdeep.intelliops.ui

import com.harshdeep.intelliops.PlatformBackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harshdeep.intelliops.data.model.Priority
import com.harshdeep.intelliops.data.model.Role
import com.harshdeep.intelliops.data.model.Task
import com.harshdeep.intelliops.data.model.TaskStatus
import com.harshdeep.intelliops.data.model.Team
import com.harshdeep.intelliops.data.model.User
import com.harshdeep.intelliops.ui.viewmodel.DashboardViewModel
import com.harshdeep.intelliops.ui.viewmodel.TasksViewModel
import kotlin.math.abs

// Enterprise Board Design System Palette
private object BoardPalette {
    val Background = Color(0xFFF4F5F7)      // N20 Neutral Canvas
    val Surface = Color(0xFFFFFFFF)         // N0 Card Surface
    val PrimaryBlue = Color(0xFF0052CC)     // B400 Brand Blue
    val PrimaryBlueHover = Color(0xFF0747A6)// B500 Deep Blue
    val SoftBlueBg = Color(0xFFDEEBFF)      // B50 Pill Background

    val TextPrimary = Color(0xFF172B4D)     // N800 Dark Navy Ink
    val TextSecondary = Color(0xFF5E6C84)   // N300 Muted Slate
    val TextSubtle = Color(0xFF7A869A)      // N200 Subtle Gray
    val BorderLight = Color(0xFFDFE1E6)     // N40 Hairline Border

    // Status Semantic Tokens
    val TodoBg = Color(0xFFDFE1E6)
    val TodoText = Color(0xFF42526E)
    val InProgressBg = Color(0xFFDEEBFF)
    val InProgressText = Color(0xFF0747A6)
    val DoneBg = Color(0xFFE3FCEF)
    val DoneText = Color(0xFF006644)
    val DoneCheck = Color(0xFF36B37E)

    // Priority Semantic Tokens
    val HighRed = Color(0xFFFF5630)
    val MidOrange = Color(0xFFFF991F)
    val LowBlue = Color(0xFF2684FF)

    // Deterministic Avatar Colors
    val AvatarColors = listOf(
        Color(0xFF0052CC),
        Color(0xFF00875A),
        Color(0xFF5243AA),
        Color(0xFFFF5630),
        Color(0xFF00B8D9),
        Color(0xFFFF991F)
    )
}

/**
 * Generates a consistent project prefix (e.g., "Engineering" -> "ENG")
 */
private fun getProjectKey(teamName: String): String {
    val clean = teamName.trim().uppercase().filter { it.isLetter() }
    return when {
        clean.length >= 3 -> clean.take(3)
        clean.isNotEmpty() -> clean.padEnd(3, 'X')
        else -> "OPS"
    }
}

/**
 * Generates a deterministic issue number from task ID (e.g., "ENG-142")
 */
private fun getIssueKey(teamName: String, taskId: String): String {
    val prefix = getProjectKey(teamName)
    val num = (abs(taskId.hashCode()) % 899) + 100
    return "$prefix-$num"
}

/**
 * Deterministic color for a user's avatar based on their name or ID
 */
private fun getAvatarColor(seed: String): Color {
    val index = abs(seed.hashCode()) % BoardPalette.AvatarColors.size
    return BoardPalette.AvatarColors[index]
}

@Composable
fun TasksScreen(currentUser: User?) {
    val dashboardViewModel: DashboardViewModel = viewModel { DashboardViewModel() }
    val tasksViewModel: TasksViewModel = viewModel { TasksViewModel() }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            dashboardViewModel.clearData()
            tasksViewModel.clearData()
        }
    }

    LaunchedEffect(currentUser?.companyId) {
        if (currentUser != null && currentUser.companyId.isNotEmpty()) {
            dashboardViewModel.loadCompanyData(currentUser.companyId)
            tasksViewModel.loadData(currentUser.companyId)
        }
    }

    if (currentUser == null) return

    var selectedTeam by remember { mutableStateOf<Team?>(null) }
    var showTeamMembersDialog by remember { mutableStateOf(false) }

    PlatformBackHandler(enabled = selectedTeam != null) {
        selectedTeam = null
    }

    if (selectedTeam == null) {
        ProjectsDirectoryScreen(
            currentUser = currentUser,
            dashboardViewModel = dashboardViewModel,
            tasksViewModel = tasksViewModel,
            onTeamClick = { selectedTeam = it }
        )
    } else {
        // Keep team reference updated from state in case name/members change
        val teams by dashboardViewModel.teams.collectAsState()
        val liveTeam = teams.find { it.id == selectedTeam!!.id } ?: selectedTeam!!

        ProjectBoardScreen(
            currentUser = currentUser,
            team = liveTeam,
            dashboardViewModel = dashboardViewModel,
            tasksViewModel = tasksViewModel,
            onBack = { selectedTeam = null },
            onShowMembers = { showTeamMembersDialog = true }
        )
    }

    if (showTeamMembersDialog && selectedTeam != null) {
        TeamMembersDialog(
            currentUser = currentUser,
            team = selectedTeam!!,
            dashboardViewModel = dashboardViewModel,
            onDismiss = { showTeamMembersDialog = false }
        )
    }
}

/**
 * Projects Directory Screen
 */
@Composable
fun ProjectsDirectoryScreen(
    currentUser: User,
    dashboardViewModel: DashboardViewModel,
    tasksViewModel: TasksViewModel,
    onTeamClick: (Team) -> Unit
) {
    val teams by dashboardViewModel.teams.collectAsState()
    val allTasks by tasksViewModel.tasks.collectAsState()
    val allUsers by dashboardViewModel.users.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var newTeamName by remember { mutableStateOf("") }
    var newTeamDesc by remember { mutableStateOf("") }

    val visibleTeams = remember(teams, currentUser, searchQuery) {
        val roleFiltered = if (currentUser.role == Role.EMPLOYEE) {
            teams.filter { team ->
                team.memberIds.contains(currentUser.id) || team.id == currentUser.teamId
            }
        } else {
            teams
        }
        if (searchQuery.isBlank()) roleFiltered
        else roleFiltered.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = BoardPalette.Background,
        topBar = {
            Surface(
                color = BoardPalette.Surface,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Projects",
                                style = MaterialTheme.typography.headlineSmall,
                                color = BoardPalette.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${visibleTeams.size} active workspaces • ${currentUser.role.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BoardPalette.TextSecondary
                            )
                        }

                        if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
                            Button(
                                onClick = { showCreateTeamDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BoardPalette.PrimaryBlue),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Project", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Project Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search projects...", color = BoardPalette.TextSubtle, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BoardPalette.TextSubtle, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = BoardPalette.TextSubtle, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BoardPalette.PrimaryBlue,
                            unfocusedBorderColor = BoardPalette.BorderLight,
                            focusedContainerColor = BoardPalette.Background.copy(alpha = 0.5f),
                            unfocusedContainerColor = BoardPalette.Background.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (visibleTeams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = BoardPalette.Surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BoardPalette.BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BoardPalette.SoftBlueBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = BoardPalette.PrimaryBlue)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching projects"
                            else if (currentUser.role == Role.EMPLOYEE) "No Assigned Projects"
                            else "No Projects Created",
                            style = MaterialTheme.typography.titleMedium,
                            color = BoardPalette.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (currentUser.role == Role.EMPLOYEE)
                                "You haven't been added to a project workspace yet. Reach out to your project lead."
                            else "Create a software or operations project to start tracking issues and sprints.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BoardPalette.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleTeams) { team ->
                    val projectTasks = allTasks.filter { it.teamId == team.id }
                    val openIssuesCount = projectTasks.count { it.status != TaskStatus.COMPLETED }
                    val projectMembers = allUsers.filter { it.teamId == team.id || team.memberIds.contains(it.id) }
                    val projectKey = getProjectKey(team.name)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTeamClick(team) },
                        color = BoardPalette.Surface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BoardPalette.BorderLight),
                        shadowElevation = 0.5.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Project Avatar Icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(getAvatarColor(team.id)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = projectKey.take(2),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = team.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = BoardPalette.TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = BoardPalette.Background,
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, BoardPalette.BorderLight)
                                        ) {
                                            Text(
                                                text = projectKey,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BoardPalette.TextSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = team.description.ifBlank { "Company-managed software project" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BoardPalette.TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = BoardPalette.BorderLight.copy(alpha = 0.6f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Open Issues Metric Badge
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (openIssuesCount > 0) BoardPalette.PrimaryBlue else BoardPalette.DoneCheck)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$openIssuesCount open issues",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BoardPalette.TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Stacked Member Avatars
                                if (projectMembers.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                        projectMembers.take(4).forEach { member ->
                                            UserAvatar(name = member.name, seed = member.id, size = 24)
                                        }
                                        if (projectMembers.size > 4) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(BoardPalette.Background)
                                                    .border(1.5.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "+${projectMembers.size - 4}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BoardPalette.TextSecondary
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
        }
    }

    if (showCreateTeamDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTeamDialog = false },
            containerColor = BoardPalette.Surface,
            shape = RoundedCornerShape(8.dp),
            title = {
                Text("Create Project", color = BoardPalette.TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Set up a new board to organize issues, assignees, and sprint progress.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BoardPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newTeamName,
                        onValueChange = { newTeamName = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newTeamDesc,
                        onValueChange = { newTeamDesc = it },
                        label = { Text("Project Description / Key Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTeamName.isNotBlank()) {
                            dashboardViewModel.createTeam(currentUser.companyId, newTeamName, newTeamDesc)
                            showCreateTeamDialog = false
                            newTeamName = ""
                            newTeamDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BoardPalette.PrimaryBlue),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("Create", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTeamDialog = false }) {
                    Text("Cancel", color = BoardPalette.TextSecondary)
                }
            }
        )
    }
}

/**
 * Active Board & Backlog View
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectBoardScreen(
    currentUser: User,
    team: Team,
    dashboardViewModel: DashboardViewModel,
    tasksViewModel: TasksViewModel,
    onBack: () -> Unit,
    onShowMembers: () -> Unit
) {
    val allTasks by tasksViewModel.tasks.collectAsState()
    val allUsers by dashboardViewModel.users.collectAsState()
    val teamTasks = remember(allTasks, team.id) { allTasks.filter { it.teamId == team.id } }
    val teamMembers = remember(allUsers, team) {
        allUsers.filter { it.teamId == team.id || team.memberIds.contains(it.id) }
    }

    // Board Filters
    var selectedStatusFilter by remember { mutableStateOf<TaskStatus?>(null) } // null = ALL
    var filterMyIssuesOnly by remember { mutableStateOf(false) }

    val filteredTasks = remember(teamTasks, selectedStatusFilter, filterMyIssuesOnly, currentUser.id) {
        teamTasks.filter { task ->
            val matchesStatus = selectedStatusFilter == null || task.status == selectedStatusFilter
            val matchesUser = !filterMyIssuesOnly ||
                    task.assigneeIds.contains(currentUser.id) ||
                    task.assigneeId == currentUser.id
            matchesStatus && matchesUser
        }
    }

    // Modal & Dialog States
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showEditTeamDialog by remember { mutableStateOf(false) }
    var editTeamName by remember { mutableStateOf(team.name) }
    var editTeamDesc by remember { mutableStateOf(team.description) }

    val projectKey = getProjectKey(team.name)
    val completedCount = teamTasks.count { it.status == TaskStatus.COMPLETED }
    val totalCount = teamTasks.size
    val progressPercent = if (totalCount > 0) (teamTasks.sumOf { it.progress } / totalCount) else 0

    Scaffold(
        containerColor = BoardPalette.Background,
        topBar = {
            Surface(
                color = BoardPalette.Surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp, bottom = 12.dp)
                ) {
                    // Breadcrumb Navigation Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Projects",
                                tint = BoardPalette.TextPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Projects / $projectKey",
                                style = MaterialTheme.typography.labelSmall,
                                color = BoardPalette.TextSecondary
                            )
                            Text(
                                text = team.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = BoardPalette.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Team Avatars Cluster
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onShowMembers() }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy((-6).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            teamMembers.take(3).forEach { member ->
                                UserAvatar(name = member.name, seed = member.id, size = 28)
                            }
                            if (teamMembers.size > 3) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(BoardPalette.Background)
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${teamMembers.size - 3}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BoardPalette.TextSecondary
                                    )
                                }
                            }
                        }

                        if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
                            IconButton(onClick = { showAddMemberDialog = true }) {
                                Icon(
                                    Icons.Default.GroupAdd,
                                    contentDescription = "Add Member",
                                    tint = BoardPalette.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = {
                                editTeamName = team.name
                                editTeamDesc = team.description
                                showEditTeamDialog = true
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Project Settings",
                                    tint = BoardPalette.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Sprint Progress Bar Strip
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE SPRINT • $completedCount of $totalCount issues done",
                                style = MaterialTheme.typography.labelSmall,
                                color = BoardPalette.TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp
                            )
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.labelSmall,
                                color = BoardPalette.PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BoardPalette.DoneCheck,
                            trackColor = BoardPalette.BorderLight
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Board Status Filter Tabs + Quick Filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BoardFilterChip(
                            label = "All",
                            count = teamTasks.size,
                            selected = selectedStatusFilter == null,
                            onClick = { selectedStatusFilter = null }
                        )
                        BoardFilterChip(
                            label = "To Do",
                            count = teamTasks.count { it.status == TaskStatus.PENDING },
                            selected = selectedStatusFilter == TaskStatus.PENDING,
                            onClick = { selectedStatusFilter = TaskStatus.PENDING }
                        )
                        BoardFilterChip(
                            label = "In Progress",
                            count = teamTasks.count { it.status == TaskStatus.IN_PROGRESS },
                            selected = selectedStatusFilter == TaskStatus.IN_PROGRESS,
                            onClick = { selectedStatusFilter = TaskStatus.IN_PROGRESS }
                        )
                        BoardFilterChip(
                            label = "Done",
                            count = teamTasks.count { it.status == TaskStatus.COMPLETED },
                            selected = selectedStatusFilter == TaskStatus.COMPLETED,
                            onClick = { selectedStatusFilter = TaskStatus.COMPLETED }
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .padding(horizontal = 2.dp),
                            color = BoardPalette.BorderLight
                        )

                        // "Only My Issues" Quick Toggle
                        FilterChip(
                            selected = filterMyIssuesOnly,
                            onClick = { filterMyIssuesOnly = !filterMyIssuesOnly },
                            label = {
                                Text(
                                    "Only My Issues",
                                    fontSize = 12.sp,
                                    fontWeight = if (filterMyIssuesOnly) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BoardPalette.SoftBlueBg,
                                selectedLabelColor = BoardPalette.PrimaryBlue,
                                containerColor = BoardPalette.Surface,
                                labelColor = BoardPalette.TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterMyIssuesOnly,
                                borderColor = BoardPalette.BorderLight,
                                selectedBorderColor = BoardPalette.PrimaryBlue
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateTaskSheet = true },
                    containerColor = BoardPalette.PrimaryBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Create Issue", fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    ) { innerPadding ->
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No issues match your filter",
                        style = MaterialTheme.typography.titleMedium,
                        color = BoardPalette.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedStatusFilter != null || filterMyIssuesOnly)
                            "Try clearing active board filters to see all project tasks."
                        else "Create your first issue to get the sprint moving.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BoardPalette.TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    BoardIssueCard(
                        task = task,
                        team = team,
                        allUsers = allUsers,
                        currentUser = currentUser,
                        onStatusChange = { newStatus, newProgress ->
                            tasksViewModel.updateProgress(
                                currentUser.companyId,
                                task.id,
                                newProgress,
                                newStatus
                            )
                        },
                        onEdit = { editingTask = it },
                        onDelete = { tasksViewModel.deleteTask(currentUser.companyId, task.id) }
                    )
                }
            }
        }
    }

    // Create Issue Modal Bottom Sheet
    if (showCreateTaskSheet) {
        IssueEditorSheet(
            titleHeader = "Create Issue",
            confirmButtonText = "Create",
            teamMembers = teamMembers,
            initialTitle = "",
            initialDescription = "",
            initialPriority = Priority.MID,
            initialAssigneeIds = emptySet(),
            onDismiss = { showCreateTaskSheet = false },
            onSave = { title, desc, priority, assignees ->
                tasksViewModel.createTask(
                    currentUser.companyId,
                    title,
                    desc,
                    assignees.toList(),
                    currentUser.id,
                    team.id,
                    priority,
                    null
                )
                showCreateTaskSheet = false
            }
        )
    }

    // Edit Issue Modal Bottom Sheet
    if (editingTask != null) {
        val target = editingTask!!
        val initialAssignees = (target.assigneeIds.ifEmpty { listOfNotNull(target.assigneeId) }).toSet()
        IssueEditorSheet(
            titleHeader = "Edit ${getIssueKey(team.name, target.id)}",
            confirmButtonText = "Save Changes",
            teamMembers = teamMembers,
            initialTitle = target.title,
            initialDescription = target.description,
            initialPriority = target.priority,
            initialAssigneeIds = initialAssignees,
            showPrioritySelector = true, // Enabled priority editing
            onDismiss = { editingTask = null },
            onSave = { title, desc, priority, assignees ->
                tasksViewModel.updateTaskDetails(
                    currentUser.companyId,
                    target.id,
                    title,
                    desc,
                    assignees.toList(),
                    priority
                )
                editingTask = null
            }
        )
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        val unassignedUsers = allUsers.filter { it.teamId == null }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            containerColor = BoardPalette.Surface,
            shape = RoundedCornerShape(8.dp),
            title = { Text("Add People to ${team.name}", color = BoardPalette.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                if (unassignedUsers.isEmpty()) {
                    Text("All company members are already assigned to teams.", color = BoardPalette.TextSecondary)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(unassignedUsers) { u ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    UserAvatar(name = u.name, seed = u.id, size = 34)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(u.name, color = BoardPalette.TextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text(u.role.name, color = BoardPalette.TextSecondary, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Button(
                                    onClick = { dashboardViewModel.assignUserToTeam(currentUser.companyId, u.id, team.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BoardPalette.PrimaryBlue),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Add", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Done", color = BoardPalette.PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Edit Project Name & Description Dialog
    if (showEditTeamDialog) {
        AlertDialog(
            onDismissRequest = { showEditTeamDialog = false },
            containerColor = BoardPalette.Surface,
            shape = RoundedCornerShape(8.dp),
            title = { Text("Project Settings", color = BoardPalette.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTeamName,
                        onValueChange = { editTeamName = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editTeamDesc,
                        onValueChange = { editTeamDesc = it },
                        label = { Text("Project Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTeamName.isNotBlank()) {
                            dashboardViewModel.updateTeamDetails(currentUser.companyId, team.id, editTeamName, editTeamDesc)
                            showEditTeamDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BoardPalette.PrimaryBlue),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditTeamDialog = false }) {
                    Text("Cancel", color = BoardPalette.TextSecondary)
                }
            }
        )
    }
}

/**
 * Issue Card with Issue Key, Priority Vector, Interactive Status Pill, and Avatars
 */
@Composable
fun BoardIssueCard(
    task: Task,
    team: Team,
    allUsers: List<User>,
    currentUser: User,
    onStatusChange: (TaskStatus, Int) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: () -> Unit
) {
    val issueKey = remember(team.name, task.id) { getIssueKey(team.name, task.id) }
    val assignees = remember(allUsers, task.assigneeIds, task.assigneeId) {
        allUsers.filter { task.assigneeIds.contains(it.id) || it.id == task.assigneeId }
    }

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var actionMenuExpanded by remember { mutableStateOf(false) }

    val isAssignee = task.assigneeIds.contains(currentUser.id) || task.assigneeId == currentUser.id
    val isTeamMember = team.memberIds.contains(currentUser.id) || currentUser.teamId == team.id || (task.teamId != null && currentUser.teamId == task.teamId)
    val canChangeStatus = currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER || isAssignee || isTeamMember
    val canManageIssue = currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canManageIssue) { onEdit(task) },
        color = BoardPalette.Surface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, BoardPalette.BorderLight),
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Issue Summary & Overflow Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.status == TaskStatus.COMPLETED) BoardPalette.TextSecondary else BoardPalette.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (canManageIssue) {
                    Box {
                        IconButton(
                            onClick = { actionMenuExpanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreHoriz,
                                contentDescription = "Issue Actions",
                                tint = BoardPalette.TextSubtle,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = actionMenuExpanded,
                            onDismissRequest = { actionMenuExpanded = false },
                            containerColor = BoardPalette.Surface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit issue", fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    actionMenuExpanded = false
                                    onEdit(task)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete issue", color = BoardPalette.HighRed, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = BoardPalette.HighRed, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    actionMenuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = BoardPalette.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Metadata Bar: [Type Icon + Issue Key]  ---  [Priority Arrow + Interactive Status Lozenge + Avatar]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Issue Type Badge + Issue Key
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF4BADE8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Task",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = issueKey,
                        style = MaterialTheme.typography.labelMedium,
                        color = BoardPalette.TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }

                // Right: Priority Indicator + Status Lozenge + Assignees
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Priority Vector Icon
                    PriorityArrowIcon(priority = task.priority)

                    // Interactive Status Lozenge
                    Box {
                        val (bgColor, textColor, label) = when (task.status) {
                            TaskStatus.PENDING -> Triple(BoardPalette.TodoBg, BoardPalette.TodoText, "TO DO")
                            TaskStatus.IN_PROGRESS -> Triple(BoardPalette.InProgressBg, BoardPalette.InProgressText, "IN PROGRESS")
                            TaskStatus.COMPLETED -> Triple(BoardPalette.DoneBg, BoardPalette.DoneText, "DONE")
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(enabled = canChangeStatus) { statusDropdownExpanded = true },
                            color = bgColor,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    color = textColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.4.sp
                                )
                                if (canChangeStatus) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Change Status",
                                        tint = textColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false },
                            containerColor = BoardPalette.Surface
                        ) {
                            StatusDropdownOption(
                                label = "TO DO",
                                bgColor = BoardPalette.TodoBg,
                                textColor = BoardPalette.TodoText,
                                isCurrent = task.status == TaskStatus.PENDING
                            ) {
                                onStatusChange(TaskStatus.PENDING, 0)
                                statusDropdownExpanded = false
                            }
                            StatusDropdownOption(
                                label = "IN PROGRESS",
                                bgColor = BoardPalette.InProgressBg,
                                textColor = BoardPalette.InProgressText,
                                isCurrent = task.status == TaskStatus.IN_PROGRESS
                            ) {
                                onStatusChange(TaskStatus.IN_PROGRESS, 50)
                                statusDropdownExpanded = false
                            }
                            StatusDropdownOption(
                                label = "DONE",
                                bgColor = BoardPalette.DoneBg,
                                textColor = BoardPalette.DoneText,
                                isCurrent = task.status == TaskStatus.COMPLETED
                            ) {
                                onStatusChange(TaskStatus.COMPLETED, 100)
                                statusDropdownExpanded = false
                            }
                        }
                    }

                    // Assignee Avatar(s)
                    if (assignees.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                            assignees.take(2).forEach { user ->
                                UserAvatar(name = user.name, seed = user.id, size = 24)
                            }
                            if (assignees.size > 2) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(BoardPalette.Background)
                                        .border(1.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${assignees.size - 2}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BoardPalette.TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(BoardPalette.Background)
                                .border(1.dp, BoardPalette.BorderLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Unassigned",
                                tint = BoardPalette.TextSubtle,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDropdownOption(
    label: String,
    bgColor: Color,
    textColor: Color,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = bgColor, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = label,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = BoardPalette.PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        onClick = onClick
    )
}

@Composable
private fun PriorityArrowIcon(priority: Priority) {
    val (icon, tint, desc) = when (priority) {
        Priority.HIGH -> Triple(Icons.Default.KeyboardArrowUp, BoardPalette.HighRed, "High Priority")
        Priority.MID -> Triple(Icons.Default.Remove, BoardPalette.MidOrange, "Medium Priority")
        Priority.LOW -> Triple(Icons.Default.KeyboardArrowDown, BoardPalette.LowBlue, "Low Priority")
    }
    Icon(
        imageVector = icon,
        contentDescription = desc,
        tint = tint,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun UserAvatar(name: String, seed: String, size: Int) {
    val initials = remember(name) {
        val parts = name.trim().split(" ").filter { it.isNotEmpty() }
        when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(getAvatarColor(seed))
            .border(1.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size * 0.42).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BoardFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() },
        color = if (selected) BoardPalette.PrimaryBlue else BoardPalette.Background,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else BoardPalette.TextSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Color.White.copy(alpha = 0.22f) else BoardPalette.BorderLight)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else BoardPalette.TextPrimary
                )
            }
        }
    }
}

/**
 * Modal Bottom Sheet for Creating and Editing Issues
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueEditorSheet(
    titleHeader: String,
    confirmButtonText: String,
    teamMembers: List<User>,
    initialTitle: String,
    initialDescription: String,
    initialPriority: Priority,
    initialAssigneeIds: Set<String>,
    showPrioritySelector: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, priority: Priority, assignees: Set<String>) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var priority by remember { mutableStateOf(initialPriority) }
    var selectedAssigneeIds by remember { mutableStateOf(initialAssigneeIds) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BoardPalette.Surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleHeader,
                    style = MaterialTheme.typography.titleLarge,
                    color = BoardPalette.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = BoardPalette.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Field
            Text(
                text = "Summary *",
                style = MaterialTheme.typography.labelMedium,
                color = BoardPalette.TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("What needs to be done?", color = BoardPalette.TextSubtle) },
                singleLine = true,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Description Field
            Text(
                text = "Description",
                style = MaterialTheme.typography.labelMedium,
                color = BoardPalette.TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Add acceptance criteria, links, or notes...", color = BoardPalette.TextSubtle) },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (showPrioritySelector) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    color = BoardPalette.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.entries.forEach { item ->
                        val isSelected = priority == item
                        val label = when (item) {
                            Priority.LOW -> "Low"
                            Priority.MID -> "Medium"
                            Priority.HIGH -> "High"
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { priority = item },
                            color = if (isSelected) BoardPalette.SoftBlueBg else BoardPalette.Background,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) BoardPalette.PrimaryBlue else BoardPalette.BorderLight
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PriorityArrowIcon(priority = item)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BoardPalette.PrimaryBlue else BoardPalette.TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Assignees Multi-Select Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assignees (${selectedAssigneeIds.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = BoardPalette.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                if (selectedAssigneeIds.isNotEmpty()) {
                    Text(
                        text = "Clear all",
                        style = MaterialTheme.typography.labelSmall,
                        color = BoardPalette.PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { selectedAssigneeIds = emptySet() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (teamMembers.isEmpty()) {
                Text(
                    text = "No members in this project yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BoardPalette.TextSubtle
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    teamMembers.forEach { member ->
                        val isSelected = selectedAssigneeIds.contains(member.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedAssigneeIds = if (isSelected) {
                                    selectedAssigneeIds - member.id
                                } else {
                                    selectedAssigneeIds + member.id
                                }
                            },
                            leadingIcon = {
                                UserAvatar(name = member.name, seed = member.id, size = 20)
                            },
                            label = {
                                Text(
                                    text = member.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BoardPalette.SoftBlueBg,
                                selectedLabelColor = BoardPalette.PrimaryBlue,
                                containerColor = BoardPalette.Background
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), description.trim(), priority, selectedAssigneeIds)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BoardPalette.PrimaryBlue),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(confirmButtonText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun TeamMembersDialog(
    currentUser: User,
    team: Team,
    dashboardViewModel: DashboardViewModel,
    onDismiss: () -> Unit
) {
    val allUsers by dashboardViewModel.users.collectAsState()
    val teamMembers = allUsers.filter { it.teamId == team.id || team.memberIds.contains(it.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BoardPalette.Surface,
        shape = RoundedCornerShape(8.dp),
        title = {
            Column {
                Text(
                    text = "Project Access",
                    style = MaterialTheme.typography.titleLarge,
                    color = BoardPalette.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${team.name} • ${teamMembers.size} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = BoardPalette.TextSecondary
                )
            }
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                items(teamMembers) { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            UserAvatar(name = member.name, seed = member.id, size = 36)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = member.name,
                                    color = BoardPalette.TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = member.role.name,
                                    color = BoardPalette.TextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
                            IconButton(
                                onClick = {
                                    dashboardViewModel.removeUserFromTeam(
                                        currentUser.companyId,
                                        team.id,
                                        member.id
                                    )
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove Member",
                                    tint = BoardPalette.HighRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = BoardPalette.PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}