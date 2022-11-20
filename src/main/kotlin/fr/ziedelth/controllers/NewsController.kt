package fr.ziedelth.controllers

import fr.ziedelth.entities.News
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.NewsReleaseEvent
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.repositories.NewsRepository
import fr.ziedelth.repositories.PlatformRepository
import fr.ziedelth.utils.RequestCache
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class NewsController(
    private val countryRepository: CountryRepository,
    private val platformRepository: PlatformRepository,
    private val newsRepository: NewsRepository,
) : IController<News>("/news") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getWithPage()
            create()
        }
    }

    private fun Route.getWithPage() {
        get("/country/{country}/page/{page}/limit/{limit}") {
            try {
                val country = call.parameters["country"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/country/$country/page/$page/limit/$limit")
                val request = RequestCache.get(uuidRequest, country, page, limit)

                if (request == null || request.isExpired()) {
                    val list = newsRepository.getByPage(country, page, limit)
                    request?.update(list) ?: RequestCache.put(uuidRequest, country, page, limit, value = list)
                }

                call.respond(RequestCache.get(uuidRequest, country, page, limit)!!.value!!)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun merge(news: News) {
        news.platform = platformRepository.find(news.platform!!.uuid) ?: throw Exception("Platform not found")
        news.country = countryRepository.find(news.country!!.uuid) ?: throw Exception("Country not found")

        if (news.isNullOrNotValid()) {
            throw Exception("News is not valid")
        }
    }

    private fun Route.create() {
        post("/multiple") {
            println("POST $prefix/multiple")

            try {
                val news = call.receive<List<News>>().filter { !newsRepository.exists("hash", it.hash!!) }
                val savedNews = mutableListOf<News>()

                news.forEach {
                    merge(it)
                    savedNews.add(newsRepository.save(it))
                }

                call.respond(HttpStatusCode.Created, savedNews)
                PluginManager.callEvent(NewsReleaseEvent(savedNews))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
