package com.harshdeep.intelliops.data.repository

import com.harshdeep.intelliops.data.model.Company
import com.harshdeep.intelliops.data.model.Role
import com.harshdeep.intelliops.data.model.Task
import com.harshdeep.intelliops.data.model.Team
import com.harshdeep.intelliops.data.model.TeamMember
import com.harshdeep.intelliops.data.model.User
import com.harshdeep.intelliops.data.model.randomUUID
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IntelliOpsRepository {
    private val firestore get() = Firebase.firestore
    private val auth get() = Firebase.auth
    
    suspend fun getCurrentUser(): User? {
        val fbUser = auth.currentUser ?: return null
        val userDoc = firestore.collection("users").document(fbUser.uid).get()
        return if (userDoc.exists) userDoc.data() else null
    }

    suspend fun getCompany(companyId: String): Company? {
        return try {
            val doc = firestore.collection("companies").document(companyId).get()
            if (doc.exists) doc.data<Company>() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createCompany(company: Company, adminUser: User): Result<Unit> {
        return try {
            val updatedUser = adminUser.copy(companyId = company.id, companyName = company.name)
            
            // Save company details
            firestore.collection("companies").document(company.id).set(company)
            // Save full user details in the root users collection
            firestore.collection("users").document(updatedUser.id).set(updatedUser)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinCompany(inviteCode: String, user: User): Result<Unit> {
        return try {
            val querySnapshot = firestore.collection("companies")
                .where { "inviteCode" equalTo inviteCode }
                .get()
            
            val companyDoc = querySnapshot.documents.firstOrNull()
                ?: return Result.failure(Exception("Invalid invite code"))
                
            val company = companyDoc.data<Company>()
            val updatedUser = user.copy(companyId = company.id, companyName = company.name)
            
            firestore.collection("users").document(updatedUser.id).set(updatedUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCompanyUsers(companyId: String): Flow<List<User>> {
        return firestore.collection("users")
            .where { "companyId" equalTo companyId }
            .snapshots
            .map { it.documents.map { doc -> doc.data<User>() } }
    }

    fun getTeams(companyId: String): Flow<List<Team>> {
        return firestore.collection("companies")
            .document(companyId)
            .collection("teams")
            .snapshots
            .map { it.documents.map { doc -> doc.data<Team>() } }
    }

    fun getTeamMembers(companyId: String, teamId: String): Flow<List<TeamMember>> {
        return firestore.collection("companies")
            .document(companyId)
            .collection("teams")
            .document(teamId)
            .collection("team_members")
            .snapshots
            .map { it.documents.map { doc -> doc.data<TeamMember>() } }
    }

    suspend fun createTeam(companyId: String, team: Team) {
        val teamId = if (team.id.isBlank()) randomUUID() else team.id
        val finalTeam = team.copy(id = teamId)
        val teamRef = firestore.collection("companies").document(companyId).collection("teams").document(teamId)
        teamRef.set(finalTeam)

        // Store members in team_members and members subcollections
        for (memberId in finalTeam.memberIds) {
            val userDoc = firestore.collection("users").document(memberId).get()
            val user = if (userDoc.exists) userDoc.data<User>() else null
            val teamMember = TeamMember(
                id = memberId,
                userId = memberId,
                name = user?.name ?: "",
                email = user?.email ?: "",
                role = user?.role ?: Role.EMPLOYEE,
                companyId = companyId,
                teamId = teamId,
                joinedAt = 0L
            )
            teamRef.collection("team_members").document(memberId).set(teamMember)
            teamRef.collection("members").document(memberId).set(teamMember)
        }
    }

    suspend fun assignUserToTeam(companyId: String, userId: String, teamId: String) {
        firestore.collection("users").document(userId).set(
            mapOf("teamId" to teamId),
            merge = true
        )
        val teamRef = firestore.collection("companies").document(companyId).collection("teams").document(teamId)
        val teamDoc = teamRef.get()
        if (teamDoc.exists) {
            val team = teamDoc.data<Team>()
            if (!team.memberIds.contains(userId)) {
                teamRef.set(team.copy(memberIds = team.memberIds + userId))
            }
        }

        // Add user to team_members and members subcollections inside teams collection
        val userDoc = firestore.collection("users").document(userId).get()
        val user = if (userDoc.exists) userDoc.data<User>() else null
        val teamMember = TeamMember(
            id = userId,
            userId = userId,
            name = user?.name ?: "",
            email = user?.email ?: "",
            role = user?.role ?: Role.EMPLOYEE,
            companyId = companyId,
            teamId = teamId,
            joinedAt = 0L
        )
        teamRef.collection("team_members").document(userId).set(teamMember)
        teamRef.collection("members").document(userId).set(teamMember)
    }

    fun getTasks(companyId: String): Flow<List<Task>> {
        return firestore.collection("companies")
            .document(companyId)
            .collection("tasks")
            .snapshots
            .map { it.documents.map { doc -> doc.data<Task>() } }
    }
    
    suspend fun createTask(companyId: String, task: Task) {
        val taskId = if (task.id.isBlank()) randomUUID() else task.id
        firestore.collection("companies")
            .document(companyId)
            .collection("tasks")
            .document(taskId)
            .set(task.copy(id = taskId))
    }
    
    suspend fun updateTaskProgress(companyId: String, taskId: String, progress: Int, status: com.harshdeep.intelliops.data.model.TaskStatus) {
        firestore.collection("companies")
            .document(companyId)
            .collection("tasks")
            .document(taskId)
            .set(
                mapOf("progress" to progress, "status" to status),
                merge = true
            )
    }

    suspend fun updateTaskDetails(companyId: String, taskId: String, title: String, description: String, assigneeIds: List<String> = emptyList(), priority: com.harshdeep.intelliops.data.model.Priority = com.harshdeep.intelliops.data.model.Priority.MID, assigneeId: String? = null) {
        firestore.collection("companies")
            .document(companyId)
            .collection("tasks")
            .document(taskId)
            .set(
                mapOf(
                    "title" to title, 
                    "description" to description, 
                    "assigneeIds" to assigneeIds,
                    "priority" to priority,
                    "assigneeId" to (assigneeId ?: assigneeIds.firstOrNull())
                ),
                merge = true
            )
    }

    suspend fun updateTeamDetails(companyId: String, teamId: String, name: String, description: String) {
        firestore.collection("companies")
            .document(companyId)
            .collection("teams")
            .document(teamId)
            .set(
                mapOf("name" to name, "description" to description),
                merge = true
            )
    }

    suspend fun updateTeamName(companyId: String, teamId: String, newName: String) {
        firestore.collection("companies")
            .document(companyId)
            .collection("teams")
            .document(teamId)
            .set(
                mapOf("name" to newName),
                merge = true
            )
    }

    suspend fun deleteTask(companyId: String, taskId: String) {
        firestore.collection("companies")
            .document(companyId)
            .collection("tasks")
            .document(taskId)
            .delete()
    }

    suspend fun deleteTeam(companyId: String, teamId: String) {
        val teamRef = firestore.collection("companies").document(companyId).collection("teams").document(teamId)
        val teamDoc = teamRef.get()
        if (teamDoc.exists) {
            val team = teamDoc.data<Team>()
            for (userId in team.memberIds) {
                firestore.collection("users").document(userId).set(
                    mapOf("teamId" to null),
                    merge = true
                )
                teamRef.collection("team_members").document(userId).delete()
                teamRef.collection("members").document(userId).delete()
            }
        }
        teamRef.delete()
    }

    suspend fun removeUserFromTeam(companyId: String, teamId: String, userId: String) {
        firestore.collection("users").document(userId).set(
            mapOf("teamId" to null),
            merge = true
        )
        val teamRef = firestore.collection("companies").document(companyId).collection("teams").document(teamId)
        val teamDoc = teamRef.get()
        if (teamDoc.exists) {
            val team = teamDoc.data<Team>()
            teamRef.set(team.copy(memberIds = team.memberIds - userId))
        }
        teamRef.collection("team_members").document(userId).delete()
        teamRef.collection("members").document(userId).delete()
    }
}