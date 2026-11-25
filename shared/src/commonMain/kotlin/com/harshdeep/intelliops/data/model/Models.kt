package com.harshdeep.intelliops.data.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

fun randomUUID(): String {
    val chars = "0123456789abcdef"
    val r = { chars[Random.nextInt(16)] }
    return "${(1..8).map{r()}.joinToString("")}-${(1..4).map{r()}.joinToString("")}-4${(1..3).map{r()}.joinToString("")}-a${(1..3).map{r()}.joinToString("")}-${(1..12).map{r()}.joinToString("")}"
}

@Serializable
data class Company(
    val id: String = randomUUID(),
    val name: String = "",
    val adminId: String = "",
    val inviteCode: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class User(
    val id: String = randomUUID(),
    val email: String = "",
    val name: String = "",
    val role: Role = Role.EMPLOYEE,
    val companyId: String = "",
    val companyName: String = "",
    val teamId: String? = null
)

enum class Role {
    ADMIN,
    MANAGER,
    EMPLOYEE
}

@Serializable
data class Team(
    val id: String = randomUUID(),
    val name: String = "",
    val leadId: String = "",
    val description: String = "",
    val memberIds: List<String> = emptyList() // Store team members here securely
)

@Serializable
data class TeamMember(
    val id: String = randomUUID(),
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: Role = Role.EMPLOYEE,
    val companyId: String = "",
    val teamId: String = "",
    val joinedAt: Long = 0L
)

@Serializable
data class Task(
    val id: String = randomUUID(),
    val title: String = "",
    val description: String = "",
    val assigneeId: String? = null,
    val assigneeIds: List<String> = emptyList(),
    val teamId: String? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val progress: Int = 0,
    val priority: Priority = Priority.MID,
    val parentTaskId: String? = null,
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val dueDate: Long? = null
)

enum class Priority {
    LOW,
    MID,
    HIGH
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}