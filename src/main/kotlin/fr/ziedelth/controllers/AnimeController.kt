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
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.BodyParam
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Delete
import fr.ziedelth.utils.routes.method.Get
import fr.ziedelth.utils.routes.method.Post
import fr.ziedelth.utils.routes.method.Put
import io.ktor.http.*
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

    @Path("/country/{country}/search/hash/{hash}")
    @Get
    private fun searchByCountryAndHash(country: String, hash: String): Response {
        val anime = animeRepository.findByHash(country, hash) ?: return Response(HttpStatusCode.NotFound)
        return Response.ok(mapOf("uuid" to anime))
    }

    @Path("/country/{country}/search/name/{name}")
    @Get
    private fun searchByCountryAndName(country: String, name: String): Response {
        return Response.ok(animeService.findByName(country, name))
    }

    @Path("/country/{country}/simulcast/{simulcast}/page/{page}/limit/{limit}")
    @Get
    private fun paginationByCountryAndSimulcast(country: String, simulcast: UUID, page: Int, limit: Int): Response {
        return Response.ok(animeService.getByPage(country, simulcast, page, limit))
    }

    @Path("/diary/country/{country}/day/{day}")
    @Get
    private fun diaryByCountryAndDay(country: String, day: Int): Response {
        var selectedDay = day
        if (selectedDay == 0) selectedDay = 7
        if (selectedDay > 7) selectedDay = 1
        return Response.ok(animeService.getDiary(country, selectedDay))
    }

    @Path("/invalid/country/{country}")
    @Get
    @Authorized
    private fun getAllInvalid(country: String): Response {
        return Response.ok(animeRepository.getInvalidAnimes(country))
    }

    @Path("/missing/page/{page}/limit/{limit}")
    @Post
    @Deprecated("Replaced by JWT at /profiles/missing/...")
    private fun paginationMissing(@BodyParam watchlist: String, page: Int, limit: Int): Response {
        return Response.ok(animeRepository.getMissingAnimes(decode(watchlist), page, limit))
    }

    @Path
    @Post
    @Authorized
    private fun save(@BodyParam anime: Anime): Response {
        val countryUuid = anime.country!!.uuid
        anime.country =
            countryRepository.find(countryUuid) ?: return Response(HttpStatusCode.BadRequest, "Country not found")

        val countryTag = anime.country!!.tag!!

        if (anime.isNullOrNotValid()) {
            Logger.warning(MISSING_PARAMETERS_MESSAGE_ERROR)
            Logger.warning(anime.toString())
            return Response(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
        }

        if (animeRepository.findOneByName(countryTag, anime.name!!)?.country?.uuid == countryUuid) {
            Logger.warning("$entityName already exists")
            return Response(HttpStatusCode.Conflict, "$entityName already exists")
        }

        val hash = anime.hash()

        if (animeRepository.findByHash(countryTag, hash) != null) {
            Logger.warning("$entityName already exists")
            return Response(HttpStatusCode.Conflict, "$entityName already exists")
        }

        anime.hashes.add(hash)
        val savedAnime = animeRepository.save(anime)
        ImageCache.cache(savedAnime.uuid, savedAnime.image!!)
        animeService.invalidateAll()
        return Response.created(savedAnime)
    }

    @Path
    @Put
    @Authorized
    private fun update(@BodyParam anime: Anime): Response {
        var savedAnime =
            animeRepository.find(anime.uuid) ?: return Response(HttpStatusCode.NotFound, ANIME_NOT_FOUND_ERROR)

        if (!anime.name.isNullOrBlank()) {
            if (animeRepository.findOneByName(savedAnime.country!!.tag!!, anime.name!!) != null) {
                return Response(HttpStatusCode.Conflict, "Another anime with the name exist!")
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
        return Response.ok(savedAnime)
    }

    @Path("/merge")
    @Put
    @Authorized
    private fun merge(@BodyParam animeIds: Array<UUID>): Response {
        // Get anime
        val animes = animeIds.mapNotNull { animeRepository.find(it) }

        if (animes.isEmpty()) {
            Logger.warning(ANIME_NOT_FOUND_ERROR)
            return Response(HttpStatusCode.NotFound, ANIME_NOT_FOUND_ERROR)
        }

        // Get all countries
        val countries = animes.map { it.country }.distinctBy { it!!.uuid }

        if (countries.size > 1) {
            Logger.warning("Anime has different countries")
            return Response(HttpStatusCode.BadRequest, "Anime has different countries")
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
        return Response.ok(savedAnime)
    }

    @Path("/{uuid}")
    @Delete
    @Authorized
    private fun deleteAnime(uuid: UUID): Response {
        val savedAnime = animeRepository.find(uuid) ?: return Response(HttpStatusCode.NotFound, ANIME_NOT_FOUND_ERROR)
        animeRepository.delete(savedAnime)
        animeService.invalidateAll()
        episodeService.invalidateAll()
        return Response(HttpStatusCode.NoContent)
    }
}
