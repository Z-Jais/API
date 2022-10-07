package fr.ziedelth.controllers

import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.RequestCache
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
            search()
            getWithPage()
            getAttachment()
            create()
        }
    }

    private fun Route.search() {
        route("/country/{country}/search") {
            get("/hash/{hash}") {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val hash = call.parameters["hash"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET $prefix/country/$country/search/hash/$hash")
                val session = Database.getSession()

                try {
                    val query = session.createQuery(
                        "SELECT a.uuid FROM Anime a JOIN a.hashes h WHERE a.country.tag = :tag AND h = :hash",
                        UUID::class.java
                    )
                    query.maxResults = 1
                    query.setParameter("tag", country)
                    query.setParameter("hash", hash)
                    val uuid = query.uniqueResult() ?: return@get call.respond(HttpStatusCode.NotFound)
                    call.respond(mapOf("uuid" to uuid))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                } finally {
                    session.close()
                }
            }

            get("/name/{name}") {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val name = call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET $prefix/country/$country/search/name/$name")
                val session = Database.getSession()

                try {
                    val query = session.createQuery(
                        "FROM Anime a WHERE a.country.tag = :tag AND LOWER(name) LIKE CONCAT('%', :name, '%') ",
                        Anime::class.java
                    )
                    query.setParameter("tag", country)
                    query.setParameter("name", name.lowercase())
                    call.respond(query.list() ?: HttpStatusCode.NotFound)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                } finally {
                    session.close()
                }
            }
        }
    }

    private fun Route.getWithPage() {
        get("/country/{country}/simulcast/{simulcast}/page/{page}/limit/{limit}") {
            val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val simulcast = call.parameters["simulcast"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = call.parameters["page"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val limit = call.parameters["limit"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            println("GET $prefix/country/$country/simulcast/$simulcast/page/$page/limit/$limit")
            val request = RequestCache.get(uuidRequest, country, page, limit, simulcast)

            if (request == null || request.isExpired()) {
                val session = Database.getSession()

                try {
                    val query = session.createQuery(
                        "FROM Anime a JOIN a.simulcasts s WHERE a.country.tag = :tag AND s.uuid = :simulcast ORDER BY a.name",
                        Anime::class.java
                    )
                    query.setParameter("tag", country)
                    query.setParameter("simulcast", UUID.fromString(simulcast))
                    query.firstResult = (limit * page) - limit
                    query.maxResults = limit
                    request?.update(query.list()) ?: RequestCache.put(uuidRequest, country, page, limit, simulcast, query.list())
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                } finally {
                    session.close()
                }
            }

            call.respond(RequestCache.get(uuidRequest, country, page, limit, simulcast)?.value ?: HttpStatusCode.NotFound)
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val anime = call.receive<Anime>()

                anime.country = CountryController.getBy("uuid", anime.country?.uuid) ?: return@post run {
                    println("Country not found")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Country not found"
                    )
                }

                if (anime.isNullOrNotValid()) {
                    println("Missing parameters")
                    println(anime)
                    call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                    return@post
                }

                if (isExists("name", anime.name)) {
                    println("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                val hash = anime.hash()
                if (contains("hashes", hash)) {
                    println("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                if (!(anime.hashes.contains(hash))) {
                    anime.hashes.add(hash!!)
                }

                val savedAnime = justSave(anime)
                ImageCache.cachingNetworkImage(savedAnime.uuid, savedAnime.image!!)
                call.respond(HttpStatusCode.Created, savedAnime)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}
