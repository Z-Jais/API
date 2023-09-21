package fr.ziedelth.controllers

import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.services.AnimeService
import fr.ziedelth.services.EpisodeService
import fr.ziedelth.utils.ImageCache
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

class AnimeController(
    private val countryRepository: CountryRepository,
    private val service: AnimeService,
    private val episodeService: EpisodeService
) :
    IController<Anime>("/animes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            search()
            getByPage()
            getAttachment()
            getMissing()
            create()
            merge()
            diary()
        }
    }

    private fun Route.search() {
        route("/country/{country}/search") {
            get("/hash/{hash}") {
                val country = call.parameters["country"]!!
                val hash = call.parameters["hash"]!!
                println("GET $prefix/country/$country/search/hash/$hash")
                val anime = service.repository.findByHash(country, hash)
                call.respond(if (anime != null) mapOf("uuid" to anime) else HttpStatusCode.NotFound)
            }

            get("/name/{name}") {
                val country = call.parameters["country"]!!
                val name = call.parameters["name"]!!
                println("GET $prefix/country/$country/search/name/$name")

                try {
                    call.respond(service.findByName(country, name))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun Route.getByPage() {
        get("/country/{country}/simulcast/{simulcast}/page/{page}/limit/{limit}") {
            try {
                val country = call.parameters["country"]!!
                val simulcast = call.parameters["simulcast"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/country/$country/simulcast/$simulcast/page/$page/limit/$limit")
                call.respond(service.getByPage(country, UUID.fromString(simulcast), page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.getMissing() {
        post("/missing/page/{page}/limit/{limit}") {
            try {
                val watchlist = call.receive<String>()
                val (page, limit) = getPageAndLimit()
                println("POST $prefix/missing/page/$page/limit/$limit")
                val filterData = decode(watchlist)
                call.respond(service.repository.getMissingAnimes(filterData, page, limit))
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

                if (service.repository.findOneByName(
                        anime.country!!.tag!!,
                        anime.name!!
                    )?.country?.uuid == anime.country!!.uuid
                ) {
                    println("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                val hash = anime.hash()

                if (service.repository.findByHash(anime.country!!.tag!!, hash) != null) {
                    println("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                anime.hashes.add(hash)
                val savedAnime = service.repository.save(anime)
                ImageCache.cachingNetworkImage(savedAnime.uuid, savedAnime.image!!)

                service.invalidateAll()
                call.respond(HttpStatusCode.Created, savedAnime)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.merge() {
        put("/merge") {
            // Get list of uuids
            val uuids = call.receive<List<String>>().map { UUID.fromString(it) }
            println("PUT $prefix/merge")
            // Get anime
            val animes = uuids.mapNotNull { service.repository.find(it) }

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
                animes.map { episodeService.repository.getAllBy("anime.uuid", it.uuid) }.flatten()
                    .distinctBy { it.uuid }
                    .toMutableSet()

            val firstAnime = animes.first()

            val savedAnime = service.repository.find(
                service.repository.save(
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
            episodeService.repository.saveAll(episodes.map { it.copy(anime = savedAnime) })

            // Delete animes
            service.repository.deleteAll(animes)

            service.invalidateAll()
            episodeService.invalidateAll()
            call.respond(HttpStatusCode.OK, savedAnime)
        }
    }

    private fun Route.diary() {
        get("/diary/country/{country}/day/{day}") {
            val country = call.parameters["country"]!!
            var day = call.parameters["day"]!!.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            if (day == 0) day = 7
            if (day > 7) day = 1
            println("GET $prefix/diary/country/$country/day/$day")
            call.respond(service.getDiary(country, day))
        }
    }
}
