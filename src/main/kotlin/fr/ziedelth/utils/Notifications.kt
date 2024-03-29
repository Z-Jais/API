package fr.ziedelth.utils

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import java.io.File
import java.io.FileInputStream

object Notifications {
    private var initialized = false

    init {
        Logger.info("Initializing Firebase")
        val file = File("data/firebase_key.json")

        if (file.exists()) {
            FirebaseApp.initializeApp(
                FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(FileInputStream(file))).build()
            )

            initialized = true
        }

        Logger.info("Firebase initialized")
    }

    fun send(list: List<Message>) {
        with(System.getenv("SEND_NOTIFICATIONS")) {
            if (this.isNullOrBlank() || this == "false") return
        }

        if (initialized) {
            FirebaseMessaging.getInstance().sendEach(list)
        }
    }
}