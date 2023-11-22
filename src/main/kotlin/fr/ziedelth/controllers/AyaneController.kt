package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.dtos.AyaneDto
import fr.ziedelth.events.AyaneReleaseEvent
import fr.ziedelth.events.RecommendationEvent
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.services.RecommendationService
import fr.ziedelth.utils.plugins.PluginManager
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.BodyParam
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Post
import io.ktor.http.*
import java.time.Duration

class AyaneController : AbstractController<AyaneDto>("/ayane") {
    @Inject
    private lateinit var animeRepository: AnimeRepository

    @Inject
    private lateinit var recommendationService: RecommendationService

    @Path
    @Post
    @Authorized
    private fun save(@BodyParam ayaneDto: AyaneDto): Response {
        if (ayaneDto.message.isBlank() || ayaneDto.images.isEmpty()) {
            return Response(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
        }

        Thread {
            PluginManager.callEvent(AyaneReleaseEvent(ayaneDto))
            Thread.currentThread().join(Duration.ofHours(3).toMillis())

            val randomAnime = animeRepository.getAll().random()
            val recommendations = recommendationService.getRecommendations(listOf(randomAnime))
            PluginManager.callEvent(RecommendationEvent(randomAnime, recommendations))
        }.start()

        return Response.created(ayaneDto)
    }
}
