package fr.ziedelth.controllers

import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.isNullOrNotValid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object AnimeController : IController<Anime>("/animes") {
    fun Routing.getAnimes() {
        route(prefix) {
            getAll()
            getByUuid()
            create()
        }
    }

    private fun Route.getByUuid() {
        get("/{uuid}") {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing parameter: uuid")
            println("GET $prefix/$uuid")

            try {
                call.respond(this@AnimeController.getByUuid(UUID.fromString(uuid)) ?: return@get call.respond(HttpStatusCode.NotFound))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val anime = call.receive<Anime>()

                if (anime.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                    return@post
                }

                if (isExists("name", anime.name!!)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                if (contains("hashes", anime.hash()!!)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                if (merge(anime)) return@post call.respond(HttpStatusCode.BadRequest, "Country not found")
                save(anime)
            } catch (e: Exception) {
                println("Error while posting $prefix : ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                return@post
            }
        }
    }

    fun merge(anime: Anime): Boolean {
        anime.country = CountryController.getBy("tag", anime.country!!.tag!!) ?: return true
        anime.hashes.add(anime.hash()!!)
        return false
    }
}
