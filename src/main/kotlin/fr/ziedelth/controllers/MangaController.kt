package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.entities.Manga
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.MangasReleaseEvent
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.repositories.MangaRepository
import fr.ziedelth.repositories.PlatformRepository
import fr.ziedelth.utils.Decoder
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.RequestCache
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

class MangaController(
    private val platformRepository: PlatformRepository,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
) : IController<Manga>("/mangas") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            search()
            getWithPage()
            getAnimeWithPage()
            getWatchlistWithPage()
            getAttachment()
            create()
        }
    }

    private fun Route.search() {
        get("/country/{country}/search/ean/{ean}") {
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

    private fun Route.getWithPage() {
        get("/country/{country}/page/{page}/limit/{limit}") {
            try {
                val country = call.parameters["country"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/country/$country/page/$page/limit/$limit")
                val request = RequestCache.get(uuidRequest, country, page, limit)

                if (request == null || request.isExpired()) {
                    val list = mangaRepository.getByPage(country, page, limit)
                    request?.update(list) ?: RequestCache.put(uuidRequest, country, page, limit, value = list)
                }

                call.respond(RequestCache.get(uuidRequest, country, page, limit)!!.value!!)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.getAnimeWithPage() {
        get("/anime/{uuid}/page/{page}/limit/{limit}") {
            try {
                val animeUuid = call.parameters["uuid"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/anime/$animeUuid/page/$page/limit/$limit")
                call.respond(mangaRepository.getByPageWithAnime(UUID.fromString(animeUuid), page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.getWatchlistWithPage() {
        post("/watchlist/page/{page}/limit/{limit}") {
            try {
                val watchlist = call.receive<String>()
                val (page, limit) = getPageAndLimit()
                println("POST $prefix/watchlist/page/$page/limit/$limit")
                val dataFromGzip =
                    Gson().fromJson(Decoder.fromGzip(watchlist), Array<String>::class.java).map { UUID.fromString(it) }
                call.respond(mangaRepository.getByPageWithList(dataFromGzip, page, limit))
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
        post("/multiple") {
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
