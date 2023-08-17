package fr.ziedelth.listeners

import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.utils.Notifications
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import java.util.*

class EpisodesRelease : Listener {
    private var lastDaySend = 0
    private var lastSend = mutableListOf<UUID>()

    @EventHandler
    fun onEpisodesRelease(event: EpisodesReleaseEvent) {
        val currentDay = Calendar.getInstance()[Calendar.DAY_OF_YEAR]

        if (currentDay != lastDaySend) {
            lastDaySend = currentDay
            lastSend.clear()
        }

        val animes =
            event.episodes.mapNotNull { it.anime }.distinctBy { it.uuid }.filter { !lastSend.contains(it.uuid) }
        if (animes.isEmpty()) return
        lastSend.addAll(animes.map { it.uuid })
        val animeNames = animes.mapNotNull { it.name }.sortedBy { it.lowercase() }
        println("Sending notification for ${animes.size} animes: ${animeNames.joinToString(", ")}")

        Notifications.send(body = animeNames.joinToString(", "))
        animes.forEach {
            Notifications.send(
                title = it.name,
                body = "Un nouvel épisode est sorti !",
                topic = it.uuid.toString()
            )
        }
    }
}
