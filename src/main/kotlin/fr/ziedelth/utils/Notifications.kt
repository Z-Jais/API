package fr.ziedelth.utils

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import java.io.File
import java.io.FileInputStream

object Notifications {
    private var initialized = false

    init {
        println("Initializing Firebase")
        val file = File("data/firebase_key.json")

        if (file.exists()) {
            FirebaseApp.initializeApp(
                FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(FileInputStream(file))).build()
            )

            initialized = true
        }

        println("Firebase initialized")
    }

    fun send(title: String? = null, body: String? = null, topic: String = "all") {
        with(System.getenv("SEND_NOTIFICATIONS")) {
            println("SEND_NOTIFICATIONS: $this")
            if (this.isNullOrBlank() || this == "false") return
        }

        if (initialized) {
            FirebaseMessaging.getInstance().send(
                Message.builder().setAndroidConfig(
                    AndroidConfig.builder().setNotification(
                        AndroidNotification.builder()
                            .setTitle(title)
                            .setBody(body).build()
                    ).build()
                ).setTopic(topic).build()
            )
        }
    }
}