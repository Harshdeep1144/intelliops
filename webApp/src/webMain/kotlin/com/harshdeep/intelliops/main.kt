package com.harshdeep.intelliops

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.harshdeep.intelliops.config.FirebaseConfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    var firebaseInitialized = false
    try {
        Firebase.initialize(
            context = null, // Some GitLive Web targets require explicit context nullification
            options = FirebaseOptions(
                applicationId = FirebaseConfig.APP_ID,
                apiKey = FirebaseConfig.API_KEY,
                projectId = FirebaseConfig.PROJECT_ID,
                databaseUrl = "",
                gcmSenderId = FirebaseConfig.MESSAGING_SENDER_ID,
                storageBucket = FirebaseConfig.STORAGE_BUCKET
            )
        )
        firebaseInitialized = true
        println("Firebase successfully initialized for web!")
    } catch (e: Exception) {
        println("Firebase init error: ${e.message}")
        // If it throws an error because it was ALREADY initialized, we still want to show the app
        if (e.message?.contains("already exists") == true) {
            firebaseInitialized = true
        }
    }

    // Force it to true just to see if the UI renders
    val webAppEnvironment = object : AppEnvironment {
        override val isFirebaseInitialized = true // Force to true for debugging
    }

    ComposeViewport {
        App(webAppEnvironment)
    }
}