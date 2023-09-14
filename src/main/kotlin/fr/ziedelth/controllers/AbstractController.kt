package fr.ziedelth.controllers

import fr.ziedelth.utils.Constant
import fr.ziedelth.utils.Decoder
import fr.ziedelth.utils.ImageCache
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.*

const val UNKNOWN_MESSAGE_ERROR = "Unknown error"
const val MISSING_PARAMETERS_MESSAGE_ERROR = "Missing parameters"

open class IController<T : Serializable>(val prefix: String) {
    data class FilterData(
        val animes: List<UUID> = listOf(),
        val episodes: List<UUID> = listOf(),
        val episodeTypes: List<UUID> = listOf(),
        val langTypes: List<UUID> = listOf(),
    )

    val entityName: String =
        ((javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>).simpleName
    val uuidRequest: UUID = UUID.randomUUID()

    fun decode(watchlist: String) = Constant.gson.fromJson(Decoder.fromGzip(watchlist), FilterData::class.java)

    fun PipelineContext<Unit, ApplicationCall>.getPageAndLimit(): Pair<Int, Int> {
        val page = call.parameters["page"]!!.toIntOrNull() ?: throw IllegalArgumentException("Page is not valid")
        val limit = call.parameters["limit"]!!.toIntOrNull() ?: throw IllegalArgumentException("Limit is not valid")

        require(!(page < 1 || limit < 1)) { "Page or limit is not valid" }
        require(limit <= 30) { "Limit is too high" }

        return Pair(page, limit)
    }

    suspend fun printError(call: ApplicationCall, e: Exception) {
        e.printStackTrace()
        call.respond(HttpStatusCode.InternalServerError, e.message ?: UNKNOWN_MESSAGE_ERROR)
    }

    fun Route.getAttachment() {
        get("/attachment/{uuid}") {
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
