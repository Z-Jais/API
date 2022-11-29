package fr.ziedelth.controllers

import fr.ziedelth.entities.Manga
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.MangasReleaseEvent
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.repositories.MangaRepository
import fr.ziedelth.repositories.PlatformRepository
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class MangaController(
    private val platformRepository: PlatformRepository,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
) : IController<Manga>("/mangas") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            search()
            getWithPage(mangaRepository)
            getAnimeWithPage(mangaRepository)
            getWatchlistWithPage(mangaRepository)
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
