package fr.ziedelth.controllers

import fr.ziedelth.dtos.AyaneDto
import fr.ziedelth.events.AyaneReleaseEvent
import fr.ziedelth.utils.plugins.PluginManager
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Post
import io.ktor.http.*

class AyaneController : AbstractController<AyaneDto>("/ayane") {
    @Path
    @Post
    @Authorized
    private fun save(ayaneDto: AyaneDto): Response {
        if (ayaneDto.message.isBlank() || ayaneDto.images.isEmpty()) {
            return Response(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
        }

        Thread {
            PluginManager.callEvent(AyaneReleaseEvent(ayaneDto))
        }.start()

        return Response.created(ayaneDto)
    }
}
