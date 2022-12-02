package fr.ziedelth.controllers

import fr.ziedelth.entities.Manga
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.MangasReleaseEvent
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.repositories.MangaRepository
import fr.ziedelth.repositories.PlatformRepository
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.plugins.PluginManager
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

private const val FOUND = "Mangas found"

class MangaController(
    private val platformRepository: PlatformRepository,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
) : IController<Manga>("/mangas") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            search()

            getWithPage(mangaRepository) {
                tags = listOf("Manga")
                summary = "Get mangas by page"
                description = "Get mangas by page"
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
                        description = FOUND
                        body<List<Manga>>()
                    }
                    HttpStatusCode.InternalServerError to {
                        description = UNKNOWN_MESSAGE_ERROR
                    }
                }
            }

            getAnimeWithPage(mangaRepository) {
                tags = listOf("Manga")
                summary = "Get mangas by anime and page"
                description = "Get mangas by anime and page"
                request {
                    pathParameter<Int>("uuid") {
                        description = "Anime uuid"
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
                        description = FOUND
                        body<List<Manga>>()
                    }
                    HttpStatusCode.InternalServerError to {
                        description = UNKNOWN_MESSAGE_ERROR
                    }
                }
            }

            getWatchlistWithPage(mangaRepository) {
                tags = listOf("Manga")
                summary = "Get watchlist mangas"
                description = "Get watchlist mangas"
                request {
                    pathParameter<Int>("page") {
                        description = PAGE
                    }
                    pathParameter<Int>("limit") {
                        description = LIMIT
                    }
                    body<String> {
                        description = "Mangas uuids encoded in GZIP"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = FOUND
                        body<List<Manga>>()
                    }
                    HttpStatusCode.InternalServerError to {
                        description = UNKNOWN_MESSAGE_ERROR
                    }
                }
            }

            getAttachment {
                tags = listOf("Manga", "Attachment")
                summary = "Get manga attachment"
                description = "Get manga attachment"
                request {
                    pathParameter<UUID>("uuid") {
                        description = "Manga uuid"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Attachment"
                        body<ByteArray>()
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Manga uuid is null or not valid"
                    }
                    HttpStatusCode.NoContent to {
                        description = "Manga attachment not found"
                    }
                }
            }

            create()
        }
    }

    private fun Route.search() {
        get("/country/{country}/search/ean/{ean}", {
            tags = listOf("Manga")
            summary = "Get manga by ean"
            description = "Get manga by ean"
            request {
                pathParameter<String>("country") {
                    description = COUNTRY_TAG
                }
                pathParameter<Long>("ean") {
                    description = "Ean of the manga"
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "Manga found"
                    body<Manga>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Ean is not valid"
                }
                HttpStatusCode.NotFound to {
                    description = "Manga not found"
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
            try {
                val country = call.parameters["country"]!!
                val ean = call.parameters["ean"]!!.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET ${prefix}/country/$country/search/ean/$ean")
                call.respond(mangaRepository.getByEAN(country, ean) ?: return@get call.respond(HttpStatusCode.NotFound))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun merge(manga: Manga) {
        manga.platform =
            platformRepository.find(manga.platform!!.uuid) ?: throw Exception("Platform not found")
        manga.anime = animeRepository.find(manga.anime!!.uuid) ?: throw Exception("Anime not found")

        if (manga.isNullOrNotValid()) {
            throw Exception("Manga is not valid")
        }
    }

    private fun Route.create() {
        post("/multiple", {
            tags = listOf("Manga")
            summary = "Create multiple mangas"
            description = "Create multiple mangas"
            request {
                body<List<Manga>> {
                    description = "Mangas to create"
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "Mangas created"
                    body<List<Manga>>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Mangas are null or not valid"
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
            println("POST $prefix/multiple")

            try {
                val mangas = call.receive<List<Manga>>().filter { !mangaRepository.exists("hash", it.hash!!) }
                val savedMangas = mutableListOf<Manga>()

                mangas.forEach {
                    merge(it)
                    val savedManga = mangaRepository.save(it)
                    savedMangas.add(savedManga)
                    ImageCache.cachingNetworkImage(savedManga.uuid, savedManga.cover!!)
                }

                call.respond(HttpStatusCode.Created, savedMangas)
                PluginManager.callEvent(MangasReleaseEvent(savedMangas))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
