package fr.ziedelth.controllers

import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.repositories.EpisodeRepository
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.RequestCache
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.put
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

private const val FOUND = "Animes found"
private const val NOT_FOUND = "No anime found"

class AnimeController(
    private val countryRepository: CountryRepository,
    private val animeRepository: AnimeRepository,
    private val episodeRepository: EpisodeRepository
) :
    IController<Anime>("/animes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            search()
            getByPage()

            getWatchlistWithPage(animeRepository) {
                tags = listOf("Anime", "Watchlist")
                summary = "Get watchlist animes"
                description = "Get watchlist animes"
                request {
                    pathParameter<Int>("page") {
                        description = PAGE
                    }
                    pathParameter<Int>("limit") {
                        description = LIMIT
                    }
                    body<String> {
                        description = "Anime ids encoded in GZIP"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Watchlist"
                        body<List<Anime>>()
                    }
                    HttpStatusCode.InternalServerError to {
                        description = UNKNOWN_MESSAGE_ERROR
                    }
                }
            }

            getAttachment {
                tags = listOf("Anime", "Attachment")
                summary = "Get anime attachment"
                description = "Get anime attachment"
                request {
                    pathParameter<UUID>("uuid") {
                        description = "Anime uuid"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Attachment"
                        body<ByteArray>()
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Anime uuid is null or not valid"
                    }
                    HttpStatusCode.NoContent to {
                        description = "Anime attachment not found"
                    }
                }
            }

            create()
            merge()
            diary()
        }
    }

    private fun Route.search() {
        route("/country/{country}/search") {
            get("/hash/{hash}", {
                tags = listOf("Anime")
                summary = "Search animes by hash"
                description = "Search animes by hash"
                request {
                    pathParameter<String>("country") {
                        description = COUNTRY_TAG
                    }
                    pathParameter<String>("hash") {
                        description = "Hash"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = FOUND
                        body<Map<String, Anime>>()
                    }
                    HttpStatusCode.NotFound to {
                        description = NOT_FOUND
                    }
                }
            }) {
                val country = call.parameters["country"]!!
                val hash = call.parameters["hash"]!!
                println("GET $prefix/country/$country/search/hash/$hash")
                val anime = animeRepository.findByHash(country, hash)
                call.respond(if (anime != null) mapOf("uuid" to anime) else HttpStatusCode.NotFound)
            }

            get("/name/{name}", {
                tags = listOf("Anime")
                summary = "Search animes by name"
                description = "Search animes by name"
                request {
                    pathParameter<String>("country") {
                        description = COUNTRY_TAG
                    }
                    pathParameter<String>("name") {
                        description = "Name"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = FOUND
                        body<List<Anime>>()
                    }
                    HttpStatusCode.NotFound to {
                        description = NOT_FOUND
                    }
                }
            }) {
                val country = call.parameters["country"]!!
                val name = call.parameters["name"]!!
                println("GET $prefix/country/$country/search/name/$name")
                call.respond(animeRepository.findByName(country, name))
            }
        }
    }

    private fun Route.getByPage() {
        get("/country/{country}/simulcast/{simulcast}/page/{page}/limit/{limit}", {
            tags = listOf("Anime")
            summary = "Get animes by page"
            description = "Get animes by page"
            request {
                pathParameter<String>("country") {
                    description = COUNTRY_TAG
                }
                pathParameter<UUID>("simulcast") {
                    description = "Simulcast UUID"
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
                    body<List<Anime>>()
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
            try {
                val country = call.parameters["country"]!!
                val simulcast = call.parameters["simulcast"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/country/$country/simulcast/$simulcast/page/$page/limit/$limit")
                val request = RequestCache.get(uuidRequest, country, page, limit, simulcast)

                if (request == null || request.isExpired()) {
                    val list = animeRepository.getByPage(country, UUID.fromString(simulcast), page, limit)
                    request?.update(list) ?: RequestCache.put(uuidRequest, country, page, limit, simulcast, list)
                }

                call.respond(RequestCache.get(uuidRequest, country, page, limit, simulcast)!!.value!!)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.create() {
        post({
            tags = listOf("Anime")
            summary = "Create an anime"
            description = "Create an anime"
            request {
                body<Anime> {
                    description = "Anime to create"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "Anime created"
                    body<Anime>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Anime is not valid or country not found"
                }
                HttpStatusCode.Conflict to {
                    description = "Anime already exists"
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
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

                if (animeRepository.findOneByName(
                        anime.country!!.tag!!,
                        anime.name!!
                    )?.country?.uuid == anime.country!!.uuid
                ) {
                    println("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                val hash = anime.hash()

                if (animeRepository.findByHash(anime.country!!.tag!!, hash) != null) {
                    println("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                anime.hashes.add(hash)
                val savedAnime = animeRepository.save(anime)
                ImageCache.cachingNetworkImage(savedAnime.uuid, savedAnime.image!!)
                call.respond(HttpStatusCode.Created, savedAnime)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.merge() {
        put("/merge", {
            tags = listOf("Anime")
            summary = "Merge animes"
            description = "Merge animes"
            request {
                body<List<UUID>> {
                    description = "Animes UUID"
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "Anime merged"
                    body<Anime>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Different countries"
                }
                HttpStatusCode.NotFound to {
                    description = NOT_FOUND
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
            // Get list of uuids
            val uuids = call.receive<List<String>>().map { UUID.fromString(it) }
            println("PUT $prefix/merge")
            // Get anime
            val animes = uuids.mapNotNull { animeRepository.find(it) }

            if (animes.isEmpty()) {
                println("Anime not found")
                call.respond(HttpStatusCode.NotFound, "Anime not found")
                return@put
            }

            // Get all countries
            val countries = animes.map { it.country }.distinctBy { it!!.uuid }

            if (countries.size > 1) {
                println("Anime has different countries")
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
                animes.map { episodeRepository.getAllBy("anime.uuid", it.uuid) }.flatten().distinctBy { it.uuid }
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

            ImageCache.cachingNetworkImage(savedAnime.uuid, savedAnime.image!!)
            episodeRepository.saveAll(episodes.map { it.copy(anime = savedAnime) })

            // Delete animes
            animeRepository.deleteAll(animes)
            call.respond(HttpStatusCode.OK, savedAnime)
        }
    }

    private fun Route.diary() {
        get("/diary/country/{country}/day/{day}", {
            tags = listOf("Anime")
            summary = "Get anime diary"
            description = "Get anime diary"
            request {
                pathParameter<String>("country") {
                    description = COUNTRY_TAG
                }
                pathParameter<Int>("day") {
                    description = "Day of the week"
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "Anime diary"
                    body<List<Anime>>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Day is not valid"
                }
            }
        }) {
            val country = call.parameters["country"]!!
            val day = call.parameters["day"]!!.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            println("GET $prefix/diary/country/$country/day/$day")
            call.respond(animeRepository.getDiary(country, day))
        }
    }
}
