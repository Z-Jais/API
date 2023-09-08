package fr.ziedelth.controllers

import fr.ziedelth.dtos.Ayane
import fr.ziedelth.events.AyaneReleaseEvent
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class AyaneController : IController<Ayane>("/ayane") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            create()
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val ayane = call.receive<Ayane>()

                if (ayane.message.isBlank() || ayane.images.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                call.respond(HttpStatusCode.Created, "OK")

                Thread {
                    PluginManager.callEvent(AyaneReleaseEvent(ayane))
                }.start()
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
