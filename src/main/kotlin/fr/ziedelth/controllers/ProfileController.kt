package fr.ziedelth.controllers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.inject.Inject
import fr.ziedelth.converters.AbstractConverter
import fr.ziedelth.dtos.ProfileDto
import fr.ziedelth.entities.Profile
import fr.ziedelth.repositories.EpisodeRepository
import fr.ziedelth.repositories.ProfileRepository
import fr.ziedelth.services.EpisodeTypeService
import fr.ziedelth.services.LangTypeService
import fr.ziedelth.utils.Constant
import fr.ziedelth.utils.routes.*
import fr.ziedelth.utils.routes.method.Delete
import fr.ziedelth.utils.routes.method.Get
import fr.ziedelth.utils.routes.method.Post
import fr.ziedelth.utils.routes.method.Put
import io.ktor.http.*
import java.io.Serializable
import java.util.*

class ProfileController : AbstractController<Serializable>("/profile") {
    @Inject
    private lateinit var episodeRepository: EpisodeRepository

    @Inject
    private lateinit var profileRepository: ProfileRepository

    @Inject
    private lateinit var episodeTypeService: EpisodeTypeService

    @Inject
    private lateinit var langTypeService: LangTypeService

    /**
     * Calculates the total duration of episodes seen based on the provided watchlist.
     *
     * @param watchlist A string containing the watchlist data.
     * @return A Response object containing the total duration of episodes seen in the watchlist.
     * @deprecated Please use the method with JWT Tokens instead.
     */
    @Path("/total-duration")
    @Post
    @Deprecated("Please use the method with JWT Tokens")
    private fun getTotalDuration(@BodyParam watchlist: String): Response {
        return Response.ok(mapOf("total-duration" to episodeRepository.getTotalDurationSeen(decode(watchlist).episodes)))
    }

    /**
     * Registers a watchlist.
     *
     * @param watchlist The watchlist to be registered.
     * @return A Response object indicating the outcome of the registration process.
     */
    @Path("/register")
    @Post
    private fun register(@BodyParam watchlist: String): Response {
        return Response.ok(mapOf("tokenUuid" to profileRepository.save(decode(watchlist)).tokenUuid))
    }

    /**
     * Logs in a user with the provided UUID string.
     *
     * @param uuid The UUID string used to identify the user.
     * @return The response containing the profile information and access token if successful, or an error message if not found.
     */
    @Path("/login")
    @Post
    private fun login(@BodyParam uuid: String): Response {
        val loggedInProfile: Profile = profileRepository.findByToken(UUID.fromString(uuid)) ?: return Response(
            HttpStatusCode.NotFound,
            "Could not find profile"
        )

        val token = JWT.create()
            .withAudience(Constant.jwtAudience)
            .withIssuer(Constant.jwtDomain)
            .withClaim("uuid", loggedInProfile.uuid.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + Constant.JWT_TOKEN_TIMEOUT))
            .sign(Algorithm.HMAC256(Constant.jwtSecret))

        val profileDto = AbstractConverter.convert(loggedInProfile, ProfileDto::class.java)
        profileDto.token = token
        return Response.ok(profileDto)
    }

    /**
     * Adds an anime or episode to the user's watchlist.
     *
     * @param jwtUser The user's JWT token.
     * @param anime The name of the anime to add to the watchlist.
     * @param episode The episode to add to the watchlist. (Optional)
     * @return A response indicating the success or failure of the operation.
     */
    @Path("/watchlist")
    @Put
    @Authenticated
    private fun addToWatchlist(@JWTUser jwtUser: UUID, @QueryParam anime: String?, @QueryParam episode: String?): Response {
        profileRepository.addToWatchlist(profileRepository.find(jwtUser)!!, anime, episode) ?: return Response(HttpStatusCode.BadRequest)
        return Response.ok()
    }

    /**
     * Deletes an anime or episode from the user's watchlist.
     *
     * @param jwtUser The user's JWT that identifies the user.
     * @param anime The name of the anime to delete from the watchlist. Optional parameter.
     * @param episode The episode of the anime to delete from the watchlist. Optional parameter.
     * @return A Response object indicating the status of the delete operation.
     */
    @Path("/watchlist")
    @Delete
    @Authenticated
    private fun deleteToWatchlist(@JWTUser jwtUser: UUID, @QueryParam anime: String?, @QueryParam episode: String?): Response {
        profileRepository.removeToWatchlist(profileRepository.find(jwtUser)!!, anime, episode) ?: return Response(HttpStatusCode.BadRequest)
        return Response.ok()
    }

    private fun getEpisodeAndLangTypesUuid(
        episodeTypes: String?,
        langTypes: String?
    ): Pair<List<UUID>, List<UUID>> {
        val episodeTypesUuid =
            if (episodeTypes.isNullOrBlank()) episodeTypeService.getAll().map { it.uuid } else episodeTypes.split(",").map { UUID.fromString(it) }
        val langTypesUuid = if (langTypes.isNullOrBlank()) langTypeService.getAll().map { it.uuid } else langTypes.split(",").map { UUID.fromString(it) }
        return Pair(episodeTypesUuid, langTypesUuid)
    }

    /**
     * Retrieves a paginated list of missing animes based on the specified filters.
     *
     * @param jwtUser The JWTUser object representing the authenticated user.
     * @param episodeTypes A comma-separated string of episode types. If null or blank, all episode types will be considered.
     * @param langTypes A comma-separated string of language types. If null or blank, all language types will be considered.
     * @param page The page number of the desired results.
     * @param limit The maximum number of results to be returned per page.
     *
     * @return A Response object containing the paginated list of missing animes.
     */
    @Path("/watchlist/animes/missing/page/{page}/limit/{limit}")
    @Get
    @Authenticated
    private fun paginationWatchlistAnimesMissing(
        @JWTUser jwtUser: UUID,
        @QueryParam episodeTypes: String?,
        @QueryParam langTypes: String?,
        page: Int,
        limit: Int
    ): Response {
        val (episodeTypesUuid, langTypesUuid) = getEpisodeAndLangTypesUuid(episodeTypes, langTypes)
        return Response.ok(profileRepository.getMissingAnimes(jwtUser, episodeTypesUuid, langTypesUuid, page, limit))
    }

    @Path("/watchlist/episodes/missing/page/{page}/limit/{limit}")
    @Get
    @Authenticated
    private fun paginationWatchlistEpisodesMissing(
        @JWTUser jwtUser: UUID,
        @QueryParam episodeTypes: String?,
        @QueryParam langTypes: String?,
        page: Int,
        limit: Int
    ): Response {
        val (episodeTypesUuid, langTypesUuid) = getEpisodeAndLangTypesUuid(episodeTypes, langTypes)
        return Response.ok(profileRepository.getMissingEpisodes(jwtUser, episodeTypesUuid, langTypesUuid, page, limit))
    }

    /**
     * Retrieves a paginated list of animes from the user's watchlist based on the specified page and limit.
     *
     * @param jwtUser The JWT user identifier.
     * @param page The page number to retrieve.
     * @param limit The maximum number of animes to retrieve per page.
     * @return A Response object containing the paginated list of animes from the user's watchlist.
     */
    @Path("/watchlist/animes/page/{page}/limit/{limit}")
    @Get
    @Authenticated
    private fun getWatchlistAnimesByPageAndLimit(@JWTUser jwtUser: UUID, page: Int, limit: Int): Response {
        return Response.ok(profileRepository.getWatchlistAnimes(jwtUser, page, limit))
    }

    /**
     * Retrieves a specific page of episodes from the watchlist based on the given page number and limit.
     *
     * @param jwtUser The authenticated JWT user UUID.
     * @param page The page number to retrieve.
     * @param limit The maximum number of episodes per page.
     * @return The response containing the requested episodes from the watchlist.
     */
    @Path("/watchlist/episodes/page/{page}/limit/{limit}")
    @Get
    @Authenticated
    private fun getWatchlistEpisodesByPageAndLimit(@JWTUser jwtUser: UUID, page: Int, limit: Int): Response {
        return Response.ok(profileRepository.getWatchlistEpisodes(jwtUser, page, limit))
    }

    @Path
    @Delete
    @Authenticated
    private fun deleteProfile(@JWTUser jwtUser: UUID): Response {
        profileRepository.delete(profileRepository.find(jwtUser)!!)
        return Response.ok()
    }
}
