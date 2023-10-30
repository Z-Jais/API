package fr.ziedelth.controllers

import fr.ziedelth.utils.Constant
import fr.ziedelth.utils.Decoder
import fr.ziedelth.utils.Logger
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.*

const val UNKNOWN_MESSAGE_ERROR = "Unknown error"
const val MISSING_PARAMETERS_MESSAGE_ERROR = "Missing parameters"

open class AbstractController<T : Serializable>(open val prefix: String) {
    data class FilterData(
        val animes: List<UUID> = listOf(), // Animes in watchlist
        val episodes: List<UUID> = listOf(), // Episodes seen
        val episodeTypes: List<UUID> = listOf(), // Episode types wanted to see
        val langTypes: List<UUID> = listOf(), // Lang types wanted to see
    )

    val entityName: String = ((javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>).simpleName

    fun decode(watchlist: String): FilterData {
        val filterData = Constant.gson.fromJson(Decoder.fromGzip(watchlist), FilterData::class.java)
        Logger.config("Episodes: ${filterData.episodes.size} - Animes: ${filterData.animes.size}")
        return filterData
    }
}
