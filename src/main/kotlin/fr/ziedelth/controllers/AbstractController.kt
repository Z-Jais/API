package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.repositories.IPageRepository
import fr.ziedelth.utils.Decoder
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.RequestCache
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.*

const val UNKNOWN_MESSAGE_ERROR = "Unknown error"
const val MISSING_PARAMETERS_MESSAGE_ERROR = "Missing parameters"

open class IController<T : Serializable>(val prefix: String) {
    val entityName: String = ((javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>).simpleName
    val uuidRequest: UUID = UUID.randomUUID()

    fun PipelineContext<Unit, ApplicationCall>.getPageAndLimit(): Pair<Int, Int> {
        val page = call.parameters["page"]!!.toIntOrNull() ?: throw IllegalArgumentException("Page is not valid")
        val limit = call.parameters["limit"]!!.toIntOrNull() ?: throw IllegalArgumentException("Limit is not valid")

        if (page < 1 || limit < 1) {
            throw IllegalArgumentException("Page or limit is not valid")
        }

        if (limit > 30) {
            throw IllegalArgumentException("Limit is too high")
        }

        return Pair(page, limit)
    }

    suspend fun printError(call: ApplicationCall, e: Exception) {
        e.printStackTrace()
        call.respond(HttpStatusCode.InternalServerError, e.message ?: UNKNOWN_MESSAGE_ERROR)
    }

    protected fun Route.getWithPage(iPageRepository: IPageRepository<T>) {
        get("/country/{country}/page/{page}/limit/{limit}") {
            try {
                val country = call.parameters["country"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/country/$country/page/$page/limit/$limit")
                val request = RequestCache.get(uuidRequest, country, page, limit)

                if (request == null || request.isExpired()) {
                    val list = iPageRepository.getByPage(country, page, limit)
                    request?.update(list) ?: RequestCache.put(uuidRequest, country, page, limit, value = list)
                }

                call.respond(RequestCache.get(uuidRequest, country, page, limit)!!.value!!)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    protected fun Route.getAnimeWithPage(iPageRepository: IPageRepository<T>) {
        get("/anime/{uuid}/page/{page}/limit/{limit}") {
            try {
                val animeUuid = call.parameters["uuid"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/anime/$animeUuid/page/$page/limit/$limit")
                call.respond(iPageRepository.getByPageWithAnime(UUID.fromString(animeUuid), page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    protected fun Route.getWatchlistWithPage(iPageRepository: IPageRepository<T>) {
        post("/watchlist/page/{page}/limit/{limit}") {
            try {
                val watchlist = call.receive<String>()
                val (page, limit) = getPageAndLimit()
                println("POST $prefix/watchlist/page/$page/limit/$limit")
                val dataFromGzip =
                    Gson().fromJson(Decoder.fromGzip(watchlist), Array<String>::class.java).map { UUID.fromString(it) }
                call.respond(iPageRepository.getByPageWithList(dataFromGzip, page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
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
            call.respondBytes(image.bytes, ContentType("image", "webp"))
        }
    }
}
