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
    suspend fun onEpisodesRelease(event: EpisodesReleaseEvent) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (currentDay != lastDaySend) {
            lastDaySend = currentDay
            lastSend.clear()
        }

        val animes = event.episodes.map { it.anime }.distinctBy { it!!.uuid }.filter { !lastSend.contains(it!!.uuid) }
        if (animes.isEmpty()) return
        lastSend.addAll(animes.map { it!!.uuid })
        val animeNames = animes.mapNotNull { it?.name }.sortedBy { it.lowercase() }
        println("Sending notification for ${animes.size} animes: ${animeNames.joinToString(", ")}")

        Notifications.send(body = animeNames.joinToString(", "))
    }
}
