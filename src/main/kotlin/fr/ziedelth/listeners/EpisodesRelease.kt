package fr.ziedelth.listeners

import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.entities.LangType
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.utils.Notifications
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import java.util.*

class EpisodesRelease : Listener {
    private var lastDaySend = 0
    private var lastSend = mutableListOf<UUID>()

    fun toString(triple: Triple<EpisodeType, Int ,LangType>): String {
        val etName = when (triple.first.name) {
            "EPISODE" -> "Épisode"
            "SPECIAL" -> "Spécial"
            "FILM" -> "Film"
            else -> "Épisode"
        }

        val ltName = when (triple.third.name) {
            "SUBTITLES" -> "VOSTFR"
            "VOICE" -> "VF"
            else -> "VOSTFR"
        }

        return "$etName ${triple.second} $ltName"
    }

    @EventHandler
    fun onEpisodesRelease(event: EpisodesReleaseEvent) {
        val currentDay = Calendar.getInstance()[Calendar.DAY_OF_YEAR]

        if (currentDay != lastDaySend) {
            lastDaySend = currentDay
            lastSend.clear()
        }

        val animes = event.episodes.map { it.anime to toString(Triple(it.episodeType!!, it.number!!, it.langType!!)) }.distinctBy { it.first?.uuid }.filter { !lastSend.contains(it.first?.uuid) }
        if (animes.isEmpty()) return
        lastSend.addAll(animes.map { it.first!!.uuid })

        val animeNames = animes.sortedBy { it.first!!.name!!.lowercase() }
        val joinToString = animeNames.joinToString(", ") { "${it.first!!.name} - ${it.second}" }
        println("Sending notification for ${animes.size} animes: $joinToString")

        Notifications.send(body = joinToString)
        animes.forEach {
            Notifications.send(
                title = it.first!!.name,
                body = "${it.second} est disponible !",
                topic = it.first!!.uuid.toString()
            )
        }
    }
}
