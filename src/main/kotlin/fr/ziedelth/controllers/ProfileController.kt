package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.repositories.EpisodeRepository
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.Serializable

class ProfileController : AbstractController<Serializable>("/profile") {
    @Inject
    private lateinit var episodeRepository: EpisodeRepository

    @APIRoute
    private fun Route.totalDuration() {
        post("/total-duration") {
            try {
                val watchlist = call.receive<String>()
                println("GET $prefix/total-duration")
                val filterData = decode(watchlist)
                call.respond(mapOf("total-duration" to episodeRepository.getTotalDurationSeen(filterData.episodes)))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }
    }
}
