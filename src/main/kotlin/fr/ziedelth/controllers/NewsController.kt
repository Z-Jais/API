package fr.ziedelth.controllers

import fr.ziedelth.entities.News
import fr.ziedelth.utils.Database
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
            val session = Database.getSession()

            try {
                val query = session.createQuery(
                    "FROM News WHERE country.tag = :tag ORDER BY releaseDate DESC",
                    News::class.java
                )
                query.setParameter("tag", country)
                query.firstResult = (limit * page) - limit
                query.maxResults = limit
                call.respond(query.list())
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            } finally {
                session.close()
            }
        }
    }

    private fun merge(news: News, checkHash: Boolean = true) {
        if (checkHash && isExists("hash", news.hash!!)) {
            throw Exception("News already exists")
        }

        news.platform = PlatformController.getBy("uuid", news.platform!!.uuid) ?: throw Exception("Platform not found")
        news.country = CountryController.getBy("uuid", news.country!!.uuid) ?: throw Exception("Country not found")
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val news = call.receive<News>()
                merge(news)
                call.respond(HttpStatusCode.Created, justSave(news))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }

        post("/multiple") {
            println("POST $prefix/multiple")

            try {
                val news = call.receive<List<News>>().filter { !isExists("hash", it.hash!!) }

                news.forEach {
                    merge(it, false)
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
