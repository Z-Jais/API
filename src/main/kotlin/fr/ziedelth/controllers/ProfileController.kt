package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.repositories.EpisodeRepository
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Post
import java.io.Serializable

class ProfileController : AbstractController<Serializable>("/profile") {
    @Inject
    private lateinit var episodeRepository: EpisodeRepository

    @Path("/total-duration")
    @Post
    private fun getTotalDuration(body: String): Response {
        val filterData = decode(body)
        return Response.ok(mapOf("total-duration" to episodeRepository.getTotalDurationSeen(filterData.episodes)))
    }
}
