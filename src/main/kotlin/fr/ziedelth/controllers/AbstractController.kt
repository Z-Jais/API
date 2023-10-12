package fr.ziedelth.controllers

import fr.ziedelth.utils.Constant
import fr.ziedelth.utils.Decoder
import fr.ziedelth.utils.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.*
import java.util.logging.Level

const val UNKNOWN_MESSAGE_ERROR = "Unknown error"
const val MISSING_PARAMETERS_MESSAGE_ERROR = "Missing parameters"

open class AbstractController<T : Serializable>(open val prefix: String) {
    data class FilterData(
        val animes: List<UUID> = listOf(), // Animes in watchlist
        val episodes: List<UUID> = listOf(), // Episodes seen
        val episodeTypes: List<UUID> = listOf(), // Episode types wanted to see
        val langTypes: List<UUID> = listOf(), // Lang types wanted to see
    )

    val entityName: String =
        ((javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>).simpleName
    val uuidRequest: UUID = UUID.randomUUID()

    fun decode(watchlist: String): FilterData {
        val filterData = Constant.gson.fromJson(Decoder.fromGzip(watchlist), FilterData::class.java)
        Logger.config("$watchlist - Episodes: ${filterData.episodes.size} - Animes: ${filterData.animes.size}")
        return filterData
    }

    suspend fun printError(call: ApplicationCall, e: Exception) {
        Logger.log(Level.SEVERE, e.message, e)
        call.respond(HttpStatusCode.InternalServerError, e.message ?: UNKNOWN_MESSAGE_ERROR)
    }

    protected fun PipelineContext<Unit, ApplicationCall>.getPageAndLimit(): Pair<Int, Int> {
        val page = call.parameters["page"]!!.toIntOrNull() ?: throw IllegalArgumentException("Page is not valid")
        val limit = call.parameters["limit"]!!.toIntOrNull() ?: throw IllegalArgumentException("Limit is not valid")

        require(!(page < 1 || limit < 1)) { "Page or limit is not valid" }
        require(limit <= 30) { "Limit is too high" }

        return Pair(page, limit)
    }

    protected fun PipelineContext<Unit, ApplicationCall>.isUnauthorized(): Deferred<Boolean> = async {
        if (!Constant.secureKey.isNullOrBlank()) {
            val authorization = call.request.headers[HttpHeaders.Authorization]

            if (Constant.secureKey != authorization) {
                Logger.warning("Unauthorized request")
                call.respond(HttpStatusCode.Unauthorized, "Secure key not equals")
                return@async true
            }
        }

        return@async false
    }
}
