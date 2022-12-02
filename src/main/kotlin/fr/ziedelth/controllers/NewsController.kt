package fr.ziedelth.controllers

import fr.ziedelth.entities.News
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.NewsReleaseEvent
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.repositories.NewsRepository
import fr.ziedelth.repositories.PlatformRepository
import fr.ziedelth.utils.plugins.PluginManager
import io.github.smiley4.ktorswaggerui.dsl.post
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
            getWithPage(newsRepository) {
                tags = listOf("News")
                summary = "Get news by page"
                description = "Get news by page"
                request {
                    pathParameter<String>("country") {
                        description = COUNTRY_TAG
                    }
                    pathParameter<Int>("page") {
                        description = PAGE
                    }
                    pathParameter<Int>("limit") {
                        description = LIMIT
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "News found"
                        body<List<News>>()
                    }
                    HttpStatusCode.InternalServerError to {
                        description = UNKNOWN_MESSAGE_ERROR
                    }
                }
            }

            create()
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
        post("/multiple", {
            tags = listOf("News")
            summary = "Create multiple news"
            description = "Create multiple news"
            request {
                body<List<News>> {
                    description = "News to create"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "News created"
                    body<List<News>>()
                }
                HttpStatusCode.BadRequest to {
                    description = "News is null or not valid"
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
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
