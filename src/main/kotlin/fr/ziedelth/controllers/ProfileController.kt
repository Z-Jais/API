package fr.ziedelth.controllers

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.repositories.EpisodeRepository
import fr.ziedelth.utils.Decoder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.Serializable
import java.util.*

class ProfileController(private val episodeRepository: EpisodeRepository) : IController<Serializable>("/profile") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            post("/total-duration") {
                try {
                    val watchlist = call.receive<String>()
                    println("GET $prefix/total-duration")

                    val dataFromGzip = Gson().fromJson(Decoder.fromGzip(watchlist), JsonObject::class.java)
                    val episodes = dataFromGzip.getAsJsonArray("episodes").map { UUID.fromString(it.asString) }

                    call.respond(mapOf("total-duration" to episodeRepository.getTotalDurationSeen(episodes)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e)
                }
            }
        }
    }
}
