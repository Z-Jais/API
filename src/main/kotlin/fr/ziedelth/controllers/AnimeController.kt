package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.repositories.EpisodeRepository
import fr.ziedelth.repositories.SimulcastRepository
import fr.ziedelth.services.AnimeService
import fr.ziedelth.services.EpisodeService
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.Logger
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

private const val ANIME_NOT_FOUND_ERROR = "Anime not found"

class AnimeController : AttachmentController<Anime>("/animes") {
    @Inject
    private lateinit var countryRepository: CountryRepository

    @Inject
    private lateinit var episodeRepository: EpisodeRepository

    @Inject
    private lateinit var episodeService: EpisodeService

    @Inject
    private lateinit var animeRepository: AnimeRepository

    @Inject
    private lateinit var animeService: AnimeService

    @Inject
    private lateinit var simulcastRepository: SimulcastRepository

    @APIRoute
    private fun Route.searchByCountryAndHash() {
        get("/country/{country}/search/hash/{hash}") {
            val country = call.parameters["country"]!!
            val hash = call.parameters["hash"]!!
            Logger.info("GET $prefix/country/$country/search/hash/$hash")
            val anime = animeRepository.findByHash(country, hash)
            call.respond(if (anime != null) mapOf("uuid" to anime) else HttpStatusCode.NotFound)
        }
    }

    @APIRoute
    private fun Route.searchByCountryAndName() {
        get("/country/{country}/search/name/{name}") {
            val country = call.parameters["country"]!!
            val name = call.parameters["name"]!!
            Logger.info("GET $prefix/country/$country/search/name/$name")

            try {
                call.respond(animeService.findByName(country, name))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @APIRoute
    private fun Route.paginationByCountryAndSimulcast() {
        get("/country/{country}/simulcast/{simulcast}/page/{page}/limit/{limit}") {
            try {
                val country = call.parameters["country"]!!
                val simulcast = call.parameters["simulcast"]!!
                val (page, limit) = getPageAndLimit()
                Logger.info("GET $prefix/country/$country/simulcast/$simulcast/page/$page/limit/$limit")
                call.respond(animeService.getByPage(country, UUID.fromString(simulcast), page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    @APIRoute
    private fun Route.paginationMissing() {
        post("/missing/page/{page}/limit/{limit}") {
            try {
                val watchlist = call.receive<String>()
                val (page, limit) = getPageAndLimit()
                Logger.info("POST $prefix/missing/page/$page/limit/$limit")
                val filterData = decode(watchlist)
                call.respond(animeRepository.getMissingAnimes(filterData, page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    @APIRoute
    private fun Route.save() {
        post {
            Logger.info("POST $prefix")
            if (isUnauthorized().await()) return@post

            try {
                val anime = call.receive<Anime>()

                anime.country = countryRepository.find(anime.country!!.uuid) ?: return@post run {
                    Logger.warning("Country not found")

                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Country not found"
                    )
                }

                if (anime.isNullOrNotValid()) {
                    Logger.warning(MISSING_PARAMETERS_MESSAGE_ERROR)
                    Logger.warning(anime.toString())
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (animeRepository.findOneByName(
                        anime.country!!.tag!!,
                        anime.name!!
                    )?.country?.uuid == anime.country!!.uuid
                ) {
                    Logger.warning("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                val hash = anime.hash()

                if (animeRepository.findByHash(anime.country!!.tag!!, hash) != null) {
                    Logger.warning("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                anime.hashes.add(hash)
                val savedAnime = animeRepository.save(anime)
                ImageCache.cache(savedAnime.uuid, savedAnime.image!!)

                animeService.invalidateAll()
                call.respond(HttpStatusCode.Created, savedAnime)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    @APIRoute
    private fun Route.update() {
        put {
            Logger.info("PUT $prefix")
            if (isUnauthorized().await()) return@put

            try {
                val anime = call.receive<Anime>()
                var savedAnime = animeRepository.find(anime.uuid)

                if (savedAnime == null) {
                    call.respond(HttpStatusCode.NotFound, ANIME_NOT_FOUND_ERROR)
                    return@put
                }

                if (!anime.name.isNullOrBlank()) {
                    if (animeRepository.findOneByName(savedAnime.country!!.tag!!, anime.name!!) != null) {
                        call.respond(HttpStatusCode.Conflict, "Another anime with the name exist!")
                        return@put
                    }

                    savedAnime.name = anime.name
                }

                if (!anime.description.isNullOrBlank()) {
                    savedAnime.description = anime.description
                }

                if (anime.simulcasts.isNotEmpty()) {
                    val savedSimulcasts = anime.simulcasts.mapNotNull { simulcastRepository.find(it.uuid) }

                    savedAnime.simulcasts.clear()
                    savedAnime.simulcasts.addAll(savedSimulcasts)
                }

                savedAnime = animeRepository.save(savedAnime)
                animeService.invalidateAll()
                episodeService.invalidateAll()
                call.respond(HttpStatusCode.OK, savedAnime)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    @APIRoute
    private fun Route.deleteAnime() {
        delete("/{uuid}") {
            try {
                val uuid = UUID.fromString(call.parameters["uuid"]!!)
                Logger.info("DELETE $prefix/$uuid")
                if (isUnauthorized().await()) return@delete
                val savedAnime = animeRepository.find(uuid)

                if (savedAnime == null) {
                    call.respond(HttpStatusCode.NotFound, ANIME_NOT_FOUND_ERROR)
                    return@delete
                }

                animeRepository.delete(savedAnime)
                animeService.invalidateAll()
                episodeService.invalidateAll()
                call.respond(HttpStatusCode.NoContent)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    @APIRoute
    private fun Route.merge() {
        put("/merge") {
            if (isUnauthorized().await()) return@put

            // Get list of uuids
            val uuids = call.receive<List<String>>().map { UUID.fromString(it) }
            Logger.info("PUT $prefix/merge")
            // Get anime
            val animes = uuids.mapNotNull { animeRepository.find(it) }

            if (animes.isEmpty()) {
                Logger.warning(ANIME_NOT_FOUND_ERROR)
                call.respond(HttpStatusCode.NotFound, ANIME_NOT_FOUND_ERROR)
                return@put
            }

            // Get all countries
            val countries = animes.map { it.country }.distinctBy { it!!.uuid }

            if (countries.size > 1) {
                Logger.warning("Anime has different countries")
                call.respond(HttpStatusCode.BadRequest, "Anime has different countries")
                return@put
            }

            // Get all hashes
            val hashes = animes.map { it.hashes }.flatten().distinct().toMutableSet()
            // Get all genres
            val genres = animes.map { it.genres }.flatten().distinctBy { it.uuid }.toMutableSet()
            // Get all simulcasts
            val simulcasts = animes.map { it.simulcasts }.flatten().distinctBy { it.uuid }.toMutableSet()
            // Get all episodes
            val episodes =
                animes.map { episodeRepository.getAllBy("anime.uuid", it.uuid) }.flatten()
                    .distinctBy { it.uuid }
                    .toMutableSet()

            val firstAnime = animes.first()

            val savedAnime = animeRepository.find(
                animeRepository.save(
                    Anime(
                        country = countries.first(),
                        name = "${animes.first().name} (${animes.size})",
                        releaseDate = firstAnime.releaseDate,
                        image = firstAnime.image,
                        description = firstAnime.description,
                        hashes = hashes,
                        genres = genres,
                        simulcasts = simulcasts
                    )
                ).uuid
            )!!

            ImageCache.cache(savedAnime.uuid, savedAnime.image!!)
            episodeRepository.saveAll(episodes.map { it.copy(anime = savedAnime) })

            // Delete animes
            animeRepository.deleteAll(animes)

            animeService.invalidateAll()
            episodeService.invalidateAll()
            call.respond(HttpStatusCode.OK, savedAnime)
        }
    }

    @APIRoute
    private fun Route.diaryByCountryAndDay() {
        get("/diary/country/{country}/day/{day}") {
            val country = call.parameters["country"]!!
            var day = call.parameters["day"]!!.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            if (day == 0) day = 7
            if (day > 7) day = 1
            Logger.info("GET $prefix/diary/country/$country/day/$day")
            call.respond(animeService.getDiary(country, day))
        }
    }

    @APIRoute
    private fun Route.getAllInvalid() {
        get("/invalid/country/{tag}") {
            try {
                val tag = call.parameters["tag"]!!
                Logger.info("GET $prefix/invalid/country/$tag")
                if (isUnauthorized().await()) return@get
                val animes = animeRepository.getInvalidAnimes(tag)
                call.respond(animes)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
