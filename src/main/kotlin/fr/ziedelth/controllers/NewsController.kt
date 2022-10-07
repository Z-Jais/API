package fr.ziedelth.controllers

import fr.ziedelth.entities.News
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.RequestCache
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object NewsController : IController<News>("/news") {
    fun Routing.getNews() {
        route(prefix) {
            getAll()
            getWithPage()
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
                        "FROM News WHERE country.tag = :tag ORDER BY releaseDate DESC",
                        News::class.java
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

    private fun merge(news: News) {
        news.platform = PlatformController.getBy("uuid", news.platform!!.uuid) ?: throw Exception("Platform not found")
        news.country = CountryController.getBy("uuid", news.country!!.uuid) ?: throw Exception("Country not found")

        if (news.isNullOrNotValid()) {
            throw Exception("News is not valid")
        }
    }

    private fun Route.create() {
        post("/multiple") {
            println("POST $prefix/multiple")

            try {
                val news = call.receive<List<News>>().filter { !isExists("hash", it.hash!!) }

                news.forEach {
                    merge(it)
                    justSave(it)
                }

                call.respond(HttpStatusCode.Created, news)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}
