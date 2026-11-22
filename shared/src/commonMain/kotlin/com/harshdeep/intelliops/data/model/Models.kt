package com.harshdeep.intelliops.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Company(
    val id: String = "",
    val name: String = "",
    val adminId: String = "",
    val inviteCode: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: Role = Role.EMPLOYEE,
    val companyId: String = "",
    val teamId: String? = null
)

enum class Role {
    ADMIN,
    MANAGER,
    EMPLOYEE
}

@Serializable
data class Team(
    val id: String = "",
    val name: String = "",
    val leadId: String = ""
)

@Serializable
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val assigneeId: String? = null,
    val teamId: String? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val progress: Int = 0,
    val createdBy: String = "",
    val createdAt: Long = 0L
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}