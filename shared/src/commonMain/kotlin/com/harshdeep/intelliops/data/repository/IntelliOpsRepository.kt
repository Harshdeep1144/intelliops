package com.harshdeep.intelliops.data.repository

import com.harshdeep.intelliops.data.model.Company
import com.harshdeep.intelliops.data.model.Task
import com.harshdeep.intelliops.data.model.Team
import com.harshdeep.intelliops.data.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IntelliOpsRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    
    // Use the current user's company ID to namespace queries
    // In a real app, this should be cached after login.
    suspend fun getCurrentUser(): User? {
        val fbUser = auth.currentUser ?: return null
        // First try to find which company the user belongs to.
        // For simplicity in MVP, we might store users at the root, or search across companies.
        // A better structure is having a root /users/{uid} that points to their companyId.
        
        // Let's assume we maintain a root collection of basic user pointers: /users/{uid} -> { companyId: "xyz" }
        // We can just fetch the user directly if we assume a flat structure or specific queries.
        // But following the schema:
        val userPtrDoc = firestore.collection("users").document(fbUser.uid).get()
        if (!userPtrDoc.exists) return null
        
        val companyId = userPtrDoc.get<String>("companyId")
        val userDoc = firestore.collection("companies").document(companyId).collection("users").document(fbUser.uid).get()
        return if (userDoc.exists) userDoc.data() else null
    }

    suspend fun createCompany(company: Company, adminUser: User): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val companyRef = firestore.collection("companies").document(company.id)
            val adminRef = companyRef.collection("users").document(adminUser.id)
            val rootUserRef = firestore.collection("users").document(adminUser.id)
            
            // Wait, GitLive SDK might not support batches identically on all targets yet.
            // Let's just use regular writes for the MVP.
            companyRef.set(company)
            adminRef.set(adminUser)
            rootUserRef.set(mapOf("companyId" to company.id))
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
                
            val companyId = companyDoc.id
            val updatedUser = user.copy(companyId = companyId)
            
            val companyRef = firestore.collection("companies").document(companyId)
            val userRef = companyRef.collection("users").document(user.id)
            val rootUserRef = firestore.collection("users").document(user.id)
            
            userRef.set(updatedUser)
            rootUserRef.set(mapOf("companyId" to companyId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTasks(companyId: String): Flow<List<Task>> {
        return firestore.collection("companies")
            .document(companyId)
            .collection("tasks")
            .snapshots
            .map { it.documents.map { doc -> doc.data<Task>() } }
    }
    
    suspend fun createTask(companyId: String, task: Task) {
        firestore.collection("companies")
            .document(companyId)
            .collection("tasks")
            .document(task.id)
            .set(task)
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
}