package com.harshdeep.intelliops.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class EmailPayload(
    val token: String,
    val type: String,
    val to: String,
    val assigneeName: String = "",
    val memberName: String = "",
    val teamName: String = "",
    val companyName: String = "",
    val title: String = "",
    val description: String = "",
    val priority: String = ""
)

object EmailNotificationService {
    private val json = Json { encodeDefaults = true }

    // Apps Script responds to POST with a 302 redirect; Ktor follows it automatically
    private val client = HttpClient {
        followRedirects = true
    }

    suspend fun notifyNewIssue(
        recipientEmail: String,
        assigneeName: String,
        teamName: String,
        title: String,
        description: String,
        priority: String
    ) {
        if (recipientEmail.isBlank()) {
            println("EmailNotificationService: recipientEmail is blank for new issue")
            return
        }
        try {
            val payload = EmailPayload(
                token = EmailConfig.SECRET_TOKEN,
                type = "NEW_ISSUE",
                to = recipientEmail,
                assigneeName = assigneeName,
                teamName = teamName,
                title = title,
                description = description,
                priority = priority
            )
            val bodyString = json.encodeToString(payload)
            println("EmailNotificationService: Sending NEW_ISSUE to $recipientEmail (Assignee: $assigneeName, Team: $teamName)")
            val response = client.post(EmailConfig.WEB_APP_URL) {
                contentType(ContentType.Application.Json)
                setBody(bodyString)
            }
            println("EmailNotificationService: NEW_ISSUE response status: ${response.status}")
        } catch (e: Exception) {
            println("EmailNotificationService: Error sending NEW_ISSUE email: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun notifyAddedToTeam(
        recipientEmail: String,
        memberName: String,
        teamName: String,
        companyName: String
    ) {
        if (recipientEmail.isBlank()) {
            println("EmailNotificationService: recipientEmail is blank for team add")
            return
        }
        try {
            val payload = EmailPayload(
                token = EmailConfig.SECRET_TOKEN,
                type = "ADDED_TO_TEAM",
                to = recipientEmail,
                memberName = memberName,
                teamName = teamName,
                companyName = companyName
            )
            val bodyString = json.encodeToString(payload)
            println("EmailNotificationService: Sending ADDED_TO_TEAM to $recipientEmail (Member: $memberName, Team: $teamName)")
            val response = client.post(EmailConfig.WEB_APP_URL) {
                contentType(ContentType.Application.Json)
                setBody(bodyString)
            }
            println("EmailNotificationService: ADDED_TO_TEAM response status: ${response.status}")
        } catch (e: Exception) {
            println("EmailNotificationService: Error sending ADDED_TO_TEAM email: ${e.message}")
            e.printStackTrace()
        }
    }
}
