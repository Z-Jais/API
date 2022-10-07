package fr.ziedelth.controllers

import fr.ziedelth.entities.Manga
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.RequestCache
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object MangaController : IController<Manga>("/mangas") {
    fun Routing.getMangas() {
        route(prefix) {
            getAll()
            getWithPage()
            getAttachment()
            create()
        }
    }

    private fun Route.getWithPage() {
        get("/country/{country}/page/{page}/limit/{limit}") {
            val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = call.parameters["page"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val limit = call.parameters["limit"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            println("GET $prefix/country/$country/page/$page/limit/$limit")
            val request = RequestCache.get(uuidRequest, country, page, limit)

            if (request == null || request.isExpired()) {
                val session = Database.getSession()

                try {
                    val query = session.createQuery(
                        "FROM Manga WHERE anime.country.tag = :tag ORDER BY releaseDate DESC, anime.name",
                        Manga::class.java
                    )
                    query.setParameter("tag", country)
                    query.firstResult = (limit * page) - limit
                    query.maxResults = limit
                    request?.update(query.list()) ?: RequestCache.put(uuidRequest, country, page, limit, value = query.list())
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                } finally {
                    session.close()
                }
            }

            call.respond(RequestCache.get(uuidRequest, country, page, limit)?.value ?: HttpStatusCode.NotFound)
        }
    }


    private fun merge(manga: Manga) {
        manga.platform =
            PlatformController.getBy("uuid", manga.platform!!.uuid) ?: throw Exception("Platform not found")
        manga.anime = AnimeController.getBy("uuid", manga.anime!!.uuid) ?: throw Exception("Anime not found")

        if (manga.isNullOrNotValid()) {
            throw Exception("Manga is not valid")
        }
    }

    private fun Route.create() {
        post("/multiple") {
            println("POST $prefix/multiple")

            try {
                val mangas = call.receive<List<Manga>>().filter { !isExists("hash", it.hash!!) }

                mangas.forEach {
                    merge(it)
                    val savedManga = justSave(it)
                    ImageCache.cachingNetworkImage(savedManga.uuid, savedManga.cover!!)
                }

                call.respond(HttpStatusCode.Created, mangas)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}
