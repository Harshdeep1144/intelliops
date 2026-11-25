package com.harshdeep.intelliops.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harshdeep.intelliops.ui.viewmodel.DashboardViewModel
import com.harshdeep.intelliops.ui.viewmodel.TasksViewModel
import com.harshdeep.intelliops.data.model.Priority
import com.harshdeep.intelliops.data.model.Role
import com.harshdeep.intelliops.data.model.Task
import com.harshdeep.intelliops.data.model.TaskStatus
import com.harshdeep.intelliops.data.model.Team
import com.harshdeep.intelliops.data.model.User
// Remove datetime since we don't have the dependency yet
// import kotlinx.datetime.Clock
// import kotlinx.datetime.TimeZone
// import kotlinx.datetime.toLocalDateTime

@Composable
fun TasksScreen(currentUser: User?) {
    val dashboardViewModel: DashboardViewModel = viewModel { DashboardViewModel() }
    val tasksViewModel: TasksViewModel = viewModel { TasksViewModel() }
    
    // Clear data when currentUser becomes null (logout)
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

    if (selectedTeam == null) {
        TeamsListScreen(
            currentUser = currentUser, 
            viewModel = dashboardViewModel,
            onTeamClick = { selectedTeam = it }
        )
    } else {
        TeamTasksScreen(
            currentUser = currentUser,
            team = selectedTeam!!,
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

@Composable
fun TeamsListScreen(currentUser: User, viewModel: DashboardViewModel, onTeamClick: (Team) -> Unit) {
    val teams by viewModel.teams.collectAsState()
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var newTeamName by remember { mutableStateOf("") }
    var newTeamDesc by remember { mutableStateOf("") }

    val visibleTeams = remember(teams, currentUser) {
        if (currentUser.role == Role.EMPLOYEE) {
            teams.filter { team ->
                team.memberIds.contains(currentUser.id) || team.id == currentUser.teamId
            }
        } else {
            teams
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F5F7)).padding(16.dp)) {
        Text("Teams (${visibleTeams.size})", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
        Text("Role: ${currentUser.role.name}", style = MaterialTheme.typography.titleSmall, color = Color(0xFF6B778C))
        
        Spacer(modifier = Modifier.height(24.dp))

        if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
            Button(
                onClick = { showCreateTeamDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Create New Team", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (visibleTeams.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentUser.role == Role.EMPLOYEE) "No Teams Joined" else "No Teams Yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF172B4D),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentUser.role == Role.EMPLOYEE) 
                                "You are not currently joined to any team. Contact your administrator or manager to be added to a team."
                                else "No teams exist in the company yet. Click 'Create New Team' above to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B778C)
                        )
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
                items(visibleTeams) { team ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onTeamClick(team) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(team.name, style = MaterialTheme.typography.titleLarge, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (team.description.isNotBlank()) {
                                Text(team.description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B778C))
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Tap to view tasks", style = MaterialTheme.typography.labelMedium, color = Color(0xFF0052CC))
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
            title = { Text("Create Team", color = Color(0xFF172B4D), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = newTeamName, onValueChange = { newTeamName = it }, label = { Text("Team Name") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newTeamDesc, onValueChange = { newTeamDesc = it }, label = { Text("Team Description") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newTeamName.isNotBlank()) {
                        viewModel.createTeam(currentUser.companyId, newTeamName, newTeamDesc)
                        showCreateTeamDialog = false
                        newTeamName = ""
                        newTeamDesc = ""
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTeamDialog = false }) { Text("Cancel", color = Color(0xFF6B778C)) }
            }
        )
    }
}

@Composable
fun TeamTasksScreen(
    currentUser: User, 
    team: Team, 
    dashboardViewModel: DashboardViewModel, 
    tasksViewModel: TasksViewModel,
    onBack: () -> Unit,
    onShowMembers: () -> Unit
) {
    val allTasks by tasksViewModel.tasks.collectAsState()
    val allUsers by dashboardViewModel.users.collectAsState()
    val teamTasks = allTasks.filter { it.teamId == team.id }
    val teamMembers = allUsers.filter { it.teamId == team.id || team.memberIds.contains(it.id) }

    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf(Priority.MID) }

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newTaskAssigneeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var assigneeDropdownExpanded by remember { mutableStateOf(false) }

    var showEditTeamDialog by remember { mutableStateOf(false) }
    var editTeamName by remember { mutableStateOf(team.name) }

    var showEditTaskDialog by remember { mutableStateOf<Task?>(null) }
    var editTaskTitle by remember { mutableStateOf("") }
    var editTaskDesc by remember { mutableStateOf("") }
    var editTaskAssigneeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var editAssigneeDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F5F7))) {
        // Top Blue Header Panel (Matching the Image)
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF0D65D9)).padding(top = 24.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(team.name, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
                        IconButton(onClick = { 
                            editTeamName = team.name
                            showEditTeamDialog = true 
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Team Overview", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Bar mock overall
                val totalProgress = if(teamTasks.isNotEmpty()) teamTasks.sumOf { it.progress } / teamTasks.size else 0
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Progress", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                    Text("$totalProgress%", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { totalProgress / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Members Area
                Text("Members", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.clickable { onShowMembers() }, horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        val displayUsers = teamMembers.take(3)
                        displayUsers.forEach { _ ->
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Gray).border(2.dp, Color(0xFF0D65D9), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (teamMembers.size > 3) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White).border(2.dp, Color(0xFF0D65D9), CircleShape), contentAlignment = Alignment.Center) {
                                Text("+${teamMembers.size - 3}", color = Color(0xFF0D65D9), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { showAddMemberDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Member", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
        
        // Tasks List Section
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Tasks", style = MaterialTheme.typography.titleMedium, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
                    Text("Today", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B778C)) // Mock Date
                }
                if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
                    TextButton(
                        onClick = { showCreateTaskDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF0052CC))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Task", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(teamTasks) { task ->
                    TaskCardRow(
                        task = task, 
                        team = team,
                        allUsers = allUsers,
                        currentUser = currentUser,
                        onUpdate = { updatedTask ->
                            tasksViewModel.updateProgress(currentUser.companyId, updatedTask.id, updatedTask.progress, updatedTask.status)
                        },
                        onDelete = {
                            tasksViewModel.deleteTask(currentUser.companyId, task.id)
                        },
                        onEdit = { taskToEdit ->
                            editTaskTitle = taskToEdit.title
                            editTaskDesc = taskToEdit.description
                            editTaskAssigneeIds = (taskToEdit.assigneeIds.ifEmpty { listOfNotNull(taskToEdit.assigneeId) }).toSet()
                            showEditTaskDialog = taskToEdit
                        }
                    )
                }
            }
        }
    }

    if (showCreateTaskDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTaskDialog = false },
            title = { Text("Create Task", color = Color(0xFF172B4D), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTaskTitle, 
                        onValueChange = { newTaskTitle = it }, 
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTaskDesc, 
                        onValueChange = { newTaskDesc = it }, 
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Multi-Assignee Dropdown
                    val selectedCreateAssignees = teamMembers.filter { newTaskAssigneeIds.contains(it.id) }
                    val createAssigneeText = when {
                        selectedCreateAssignees.isEmpty() -> "Unassigned"
                        selectedCreateAssignees.size == 1 -> selectedCreateAssignees.first().name
                        else -> "${selectedCreateAssignees.size} Assignees (${selectedCreateAssignees.joinToString(", ") { it.name }})"
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = createAssigneeText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assignees (Multiple)") },
                            trailingIcon = {
                                Icon(
                                    if (assigneeDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Assignees"
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { assigneeDropdownExpanded = !assigneeDropdownExpanded }
                        )
                        DropdownMenu(
                            expanded = assigneeDropdownExpanded,
                            onDismissRequest = { assigneeDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Unassigned (Clear All)", color = Color(0xFF6B778C), fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    newTaskAssigneeIds = emptySet()
                                }
                            )
                            teamMembers.forEach { member ->
                                val isSelected = newTaskAssigneeIds.contains(member.id)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0D65D9)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = member.name.take(1).uppercase(),
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(member.name, color = Color(0xFF172B4D), fontWeight = FontWeight.SemiBold)
                                                Text(member.email.ifEmpty { member.role.name }, color = Color(0xFF6B778C), style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    },
                                    onClick = {
                                        newTaskAssigneeIds = if (isSelected) newTaskAssigneeIds - member.id else newTaskAssigneeIds + member.id
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        tasksViewModel.createTask(currentUser.companyId, newTaskTitle, newTaskDesc, newTaskAssigneeIds.toList(), currentUser.id, team.id, newTaskPriority, null)
                        showCreateTaskDialog = false
                        newTaskTitle = ""
                        newTaskDesc = ""
                        newTaskAssigneeIds = emptySet()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTaskDialog = false }) { Text("Cancel", color = Color(0xFF6B778C)) }
            }
        )
    }

    if (showEditTaskDialog != null) {
        AlertDialog(
            onDismissRequest = { showEditTaskDialog = null },
            title = { Text("Edit Task", color = Color(0xFF172B4D), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTaskTitle, 
                        onValueChange = { editTaskTitle = it }, 
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTaskDesc, 
                        onValueChange = { editTaskDesc = it }, 
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Multi-Assignee Dropdown for Edit Task
                    val selectedEditAssignees = teamMembers.filter { editTaskAssigneeIds.contains(it.id) }
                    val editAssigneeText = when {
                        selectedEditAssignees.isEmpty() -> "Unassigned"
                        selectedEditAssignees.size == 1 -> selectedEditAssignees.first().name
                        else -> "${selectedEditAssignees.size} Assignees (${selectedEditAssignees.joinToString(", ") { it.name }})"
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editAssigneeText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assignees (Multiple)") },
                            trailingIcon = {
                                Icon(
                                    if (editAssigneeDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Assignees"
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { editAssigneeDropdownExpanded = !editAssigneeDropdownExpanded }
                        )
                        DropdownMenu(
                            expanded = editAssigneeDropdownExpanded,
                            onDismissRequest = { editAssigneeDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Unassigned (Clear All)", color = Color(0xFF6B778C), fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    editTaskAssigneeIds = emptySet()
                                }
                            )
                            teamMembers.forEach { member ->
                                val isSelected = editTaskAssigneeIds.contains(member.id)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0D65D9)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = member.name.take(1).uppercase(),
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(member.name, color = Color(0xFF172B4D), fontWeight = FontWeight.SemiBold)
                                                Text(member.email.ifEmpty { member.role.name }, color = Color(0xFF6B778C), style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    },
                                    onClick = {
                                        editTaskAssigneeIds = if (isSelected) editTaskAssigneeIds - member.id else editTaskAssigneeIds + member.id
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editTaskTitle.isNotBlank()) {
                        tasksViewModel.updateTaskDetails(currentUser.companyId, showEditTaskDialog!!.id, editTaskTitle, editTaskDesc, editTaskAssigneeIds.toList())
                        showEditTaskDialog = null
                        editTaskTitle = ""
                        editTaskDesc = ""
                        editTaskAssigneeIds = emptySet()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditTaskDialog = null }) { Text("Cancel", color = Color(0xFF6B778C)) }
            }
        )
    }

    if (showAddMemberDialog) {
        val unassignedUsers = allUsers.filter { it.teamId == null }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Members", color = Color(0xFF172B4D), fontWeight = FontWeight.Bold) },
            text = {
                if (unassignedUsers.isEmpty()) {
                    Text("No available users in the company.")
                } else {
                    LazyColumn {
                        items(unassignedUsers) { u ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(u.name, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
                                    Text(u.role.name, color = Color(0xFF6B778C), style = MaterialTheme.typography.bodySmall)
                                }
                                Button(onClick = { dashboardViewModel.assignUserToTeam(currentUser.companyId, u.id, team.id) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddMemberDialog = false }) { Text("Done") }
            }
        )
    }

    if (showEditTeamDialog) {
        AlertDialog(
            onDismissRequest = { showEditTeamDialog = false },
            title = { Text("Edit Team Name", color = Color(0xFF172B4D), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editTeamName,
                    onValueChange = { editTeamName = it },
                    label = { Text("Team Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (editTeamName.isNotBlank() && editTeamName != team.name) {
                        dashboardViewModel.updateTeamName(currentUser.companyId, team.id, editTeamName)
                        showEditTeamDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditTeamDialog = false }) { Text("Cancel", color = Color(0xFF6B778C)) }
            }
        )
    }
}

@Composable
fun TaskCardRow(
    task: Task, 
    team: Team,
    allUsers: List<User>, 
    currentUser: User, 
    onUpdate: (Task) -> Unit, 
    onDelete: () -> Unit,
    onEdit: (Task) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(task.description, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    val priorityColor = when (task.priority) {
                        Priority.LOW -> Color(0xFFE3FCEF) to Color(0xFF006644)
                        Priority.MID -> Color(0xFFFFF0B3) to Color(0xFFFF991F)
                        Priority.HIGH -> Color(0xFFFFEBE6) to Color(0xFFBF2600)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(priorityColor.first).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(task.priority.name, color = priorityColor.second, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val (statusBg, statusTextC, statusText) = when (task.status) {
                        TaskStatus.PENDING -> Triple(Color(0xFFDFE1E6), Color(0xFF42526E), "TO DO")
                        TaskStatus.IN_PROGRESS -> Triple(Color(0xFFDEEBFF), Color(0xFF0747A6), "IN PROGRESS")
                        TaskStatus.COMPLETED -> Triple(Color(0xFFE3FCEF), Color(0xFF006644), "DONE")
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(statusBg).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(statusText, color = statusTextC, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(task.title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val assignees = allUsers.filter { task.assigneeIds.contains(it.id) || it.id == task.assigneeId }
                    if (assignees.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                            assignees.take(3).forEach { assigneeUser ->
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0D65D9))
                                        .border(1.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = assigneeUser.name.take(1).uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = assignees.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6B778C),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color.Gray), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unassigned", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B778C))
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            var statusExpanded by remember { mutableStateOf(false) }
            var menuExpanded by remember { mutableStateOf(false) }
            val isAssignee = task.assigneeIds.contains(currentUser.id) || task.assigneeId == currentUser.id
            val isTeamMember = team.memberIds.contains(currentUser.id) || currentUser.teamId == team.id || (task.teamId != null && currentUser.teamId == task.teamId)
            val hasPermission = currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER || isAssignee || isTeamMember

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    IconButton(onClick = { if (hasPermission) statusExpanded = true }) {
                        if (task.status == TaskStatus.COMPLETED) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF36B37E), modifier = Modifier.size(28.dp))
                        } else if (task.status == TaskStatus.IN_PROGRESS) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "In Progress", tint = Color(0xFF0052CC), modifier = Modifier.size(28.dp))
                        } else {
                            Icon(Icons.Outlined.Circle, contentDescription = "To Do", tint = Color(0xFFDFE1E6), modifier = Modifier.size(28.dp))
                        }
                    }
                    
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        DropdownMenuItem(text = { Text("TO DO") }, onClick = { onUpdate(task.copy(status = TaskStatus.PENDING, progress = 0)); statusExpanded = false })
                        DropdownMenuItem(text = { Text("IN PROGRESS") }, onClick = { onUpdate(task.copy(status = TaskStatus.IN_PROGRESS, progress = 50)); statusExpanded = false })
                        DropdownMenuItem(text = { Text("DONE") }, onClick = { onUpdate(task.copy(status = TaskStatus.COMPLETED, progress = 100)); statusExpanded = false })
                    }
                }
                
                if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFF6B778C))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Edit Task") }, onClick = { menuExpanded = false; onEdit(task) })
                            DropdownMenuItem(text = { Text("Delete Task", color = Color.Red) }, onClick = { menuExpanded = false; onDelete() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamMembersDialog(currentUser: User, team: Team, dashboardViewModel: DashboardViewModel, onDismiss: () -> Unit) {
    val allUsers by dashboardViewModel.users.collectAsState()
    val teamMembers = allUsers.filter { it.teamId == team.id || team.memberIds.contains(it.id) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${team.name} Members", color = Color(0xFF172B4D), fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn {
                items(teamMembers) { member ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(member.name, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
                                Text(member.role.name, color = Color(0xFF6B778C), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (currentUser.role == Role.ADMIN || currentUser.role == Role.MANAGER) {
                            IconButton(onClick = { dashboardViewModel.removeUserFromTeam(currentUser.companyId, team.id, member.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFFF5630))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}