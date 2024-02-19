package fr.ziedelth.controllers

import fr.ziedelth.dtos.CalendarDto
import fr.ziedelth.events.CalendarReleaseEvent
import fr.ziedelth.utils.plugins.PluginManager
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.BodyParam
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Post
import io.ktor.http.*

class CalendarController : AbstractController<CalendarDto>("/calendar") {
    @Path
    @Post
    @Authorized
    private fun save(@BodyParam calendarDto: CalendarDto): Response {
        if (calendarDto.message.isBlank() || calendarDto.images.isEmpty()) {
            return Response(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
        }

        PluginManager.callEvent(CalendarReleaseEvent(calendarDto))
        return Response.created(calendarDto)
    }
}
