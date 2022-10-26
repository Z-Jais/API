package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.entities.Anime
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.Encoder
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import jakarta.persistence.Tuple
import java.util.UUID

object NotificationController : IController<Anime>("/notifications") {
    fun Routing.getNotifications() {
        route(prefix) {
            webSocket {
                println("New websocket connection")

                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val (country, lastCheck) = frame.readText().split(";")
                    println("WEBSOCKET $prefix/country/$country/lastCheck/$lastCheck")
                    val session = Database.getSession()

                    try {
                        val query = session.createQuery(
                            "SELECT DISTINCT anime.uuid, anime.name FROM Episode episode WHERE episode.anime.country.tag = :tag AND to_timestamp(episode.releaseDate, 'YYYY-MM-DD`T`HH24:MI:SS') <= to_timestamp(:lastCheck, 'YYYY-MM-DD`T`HH24:MI:SS') AND to_date(episode.releaseDate, 'YYYY-MM-DD`T`HH24:MI:SS') = to_date(:lastCheck, 'YYYY-MM-DD`T`HH24:MI:SS') ORDER BY episode.anime.name ASC",
                            Tuple::class.java
                        )
                        query.setParameter("tag", country)
                        query.setParameter("lastCheck", lastCheck)
                        val list = query.list().map { mapOf("uuid" to it[0], "name" to it[1]) }
//                        send(Encoder.toGzip(Gson().toJson(list)))
                        send(Gson().toJson(list))
                        close(CloseReason(CloseReason.Codes.NORMAL, "OK"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        send(e.message ?: "Unknown error")
                        close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Internal error"))
                    } finally {
                        session.close()
                    }
                }
            }
        }
    }
}
