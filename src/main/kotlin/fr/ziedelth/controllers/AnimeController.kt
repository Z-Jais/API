package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.utils.Decoder
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.RequestCache
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.util.*

class AnimeController(private val countryRepository: CountryRepository, private val animeRepository: AnimeRepository) :
    IController<Anime>("/animes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            search()
            getWithPage()
            getWatchlistWithPage()
            getAttachment()
            create()
            merge()
        }
    }

    private fun Route.search() {
        route("/country/{country}/search") {
            get("/hash/{hash}") {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val hash = call.parameters["hash"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET $prefix/country/$country/search/hash/$hash")
                call.respond(mapOf("uuid" to animeRepository.findByHash(country, hash)))
            }

            get("/name/{name}") {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val name = call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET $prefix/country/$country/search/name/$name")
                call.respond(animeRepository.findByName(country, name))
            }
        }
    }

    private fun Route.getWithPage() {
        get("/country/{country}/simulcast/{simulcast}/page/{page}/limit/{limit}") {
            try {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val simulcast = call.parameters["simulcast"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/country/$country/simulcast/$simulcast/page/$page/limit/$limit")
                val request = RequestCache.get(uuidRequest, country, page, limit, simulcast)

                if (request == null || request.isExpired()) {
                    val list = animeRepository.getByPage(country, UUID.fromString(simulcast), page, limit)
                    request?.update(list) ?: RequestCache.put(uuidRequest, country, page, limit, simulcast, list)
                }

                call.respond(
                    RequestCache.get(uuidRequest, country, page, limit, simulcast)?.value ?: HttpStatusCode.NotFound
                )
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
                call.respond(animeRepository.findAllByPage(dataFromGzip, page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val anime = call.receive<Anime>()

                anime.country = countryRepository.find(anime.country!!.uuid) ?: return@post run {
                    println("Country not found")

                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Country not found"
                    )
                }

                if (anime.isNullOrNotValid()) {
                    println(MISSING_PARAMETERS_MESSAGE_ERROR)
                    println(anime)
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (animeRepository.exists("name", anime.name)) {
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

                val savedAnime = animeRepository.save(anime)
                ImageCache.cachingNetworkImage(savedAnime.uuid, savedAnime.image!!)
                call.respond(HttpStatusCode.Created, savedAnime)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.merge() {
        put("/merge") {
//            // Get list of uuids
//            val uuids = call.receive<List<String>>().map { UUID.fromString(it) }
//            println("PUT $prefix/merge")
//            // Get anime
//            val animes = uuids.mapNotNull { getBy("uuid", it) }
//
//            if (animes.isEmpty()) {
//                println("Anime not found")
//                call.respond(HttpStatusCode.NotFound, "Anime not found")
//                return@put
//            }
//
//            // Get all countries
//            val countries = animes.map { it.country }.distinctBy { it?.uuid }
//
//            if (countries.size > 1) {
//                println("Anime has different countries")
//                call.respond(HttpStatusCode.BadRequest, "Anime has different countries")
//                return@put
//            }
//
//            // Get all hashes
//            val hashes = animes.map { it.hashes }.flatten().distinct().toMutableSet()
//            // Get all genres
//            val genres = animes.map { it.genres }.flatten().distinctBy { it.uuid }.toMutableSet()
//            // Get all simulcasts
//            val simulcasts = animes.map { it.simulcasts }.flatten().distinctBy { it.uuid }.toMutableSet()
//            // Get all episodes
//            val episodes =
//                animes.map { EpisodeController.getAllBy("anime.uuid", it.uuid) }.flatten().distinctBy { it.uuid }
//                    .toMutableSet()
//            // Get all mangas
//            val mangas = animes.map { MangaController.getAllBy("anime.uuid", it.uuid) }.flatten().distinctBy { it.uuid }
//                .toMutableSet()
//
//            val firstAnime = animes.first()
//            val mergedAnime = Anime(
//                country = countries.first(),
//                name = "${animes.first().name} (${animes.size})",
//                releaseDate = firstAnime.releaseDate,
//                image = firstAnime.image,
//                description = firstAnime.description,
//                hashes = hashes,
//                genres = genres,
//                simulcasts = simulcasts
//            )
//
//            val savedAnime = justSave(mergedAnime)
//            ImageCache.cachingNetworkImage(savedAnime.uuid, savedAnime.image!!)
//            episodes.map { it.copy(anime = savedAnime) }.map { EpisodeController.justSave(it) }
//            mangas.map { it.copy(anime = savedAnime) }.map { MangaController.justSave(it) }
//
//            // Delete animes
//            val session = Database.getSession()
//            val transaction = session.beginTransaction()
//
//            try {
//                session.remove(animes)
//                transaction.commit()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                println("Error while deleting $prefix : ${e.message}")
//                transaction.rollback()
//                throw e
//            } finally {
//                session.close()
//            }
        }
    }
}
