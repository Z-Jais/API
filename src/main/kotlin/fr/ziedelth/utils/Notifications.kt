package fr.ziedelth.utils

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.gson.Gson
import fr.ziedelth.entities.Notification
import io.ktor.websocket.*
import java.io.File
import java.io.FileInputStream
import java.util.*

object Notifications {
    class Connection(val session: DefaultWebSocketSession) {
        val id: UUID = UUID.randomUUID()
    }

    val connections: MutableSet<Connection> = Collections.synchronizedSet(LinkedHashSet())

    init {
        println("Initializing Firebase")
        val file = File("firebase_key.json")

        if (file.exists()) {
            FirebaseApp.initializeApp(
                FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(FileInputStream(file))).build()
            )
        }

        println("Firebase initialized")
    }

    suspend fun send(title: String? = null, body: String? = null) {
        val json = Gson().toJson(Notification(title, body))

        FirebaseMessaging.getInstance().send(
            Message.builder().setAndroidConfig(
                AndroidConfig.builder().setNotification(
                    AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(body).build()
                ).build()
            ).setTopic("all").build()
        )

        connections.forEach { it.session.send(Frame.Text(json)) }
    }

    fun addConnection(session: DefaultWebSocketSession): Connection {
        val connection = Connection(session)
        connections += connection
        return connection
    }

    fun removeConnection(connection: Connection) {
        connections -= connection
    }
}