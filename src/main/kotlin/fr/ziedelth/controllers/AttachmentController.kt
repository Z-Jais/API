package fr.ziedelth.controllers

import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.routes.APIIgnore
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.Serializable
import java.util.*

@APIIgnore
open class AttachmentController<T : Serializable>(override val prefix: String) : AbstractController<T>(prefix) {
    fun Route.attachmentByUUID() {
        route("/attachment/{uuid}") {
            install(CachingHeaders) {
                options { _, _ -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 172800)) }
            }

            get {
                val string = call.parameters["uuid"]!!
                val uuidRegex =
                    "^[0-9(a-f|A-F)]{8}-[0-9(a-f|A-F)]{4}-4[0-9(a-f|A-F)]{3}-[89ab][0-9(a-f|A-F)]{3}-[0-9(a-f|A-F)]{12}\$".toRegex()

                if (!uuidRegex.matches(string)) {
                    println("GET $prefix/attachment/$string : Invalid UUID")
                    return@get call.respond(HttpStatusCode.BadRequest)
                }

                val uuid = UUID.fromString(string)
                println("GET ${prefix}/attachment/$uuid")

                if (!ImageCache.contains(uuid)) {
                    println("Attachment $uuid not found")
                    call.respond(HttpStatusCode.NoContent)
                    return@get
                }

                val image = ImageCache.get(uuid)!!
                println("Attachment $uuid found (${image.bytes.size} bytes)")
                call.respondBytes(image.bytes, ContentType("image", image.type))
            }
        }
    }
}