package fr.ziedelth.listeners

import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.utils.Notifications
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener

class EpisodesRelease : Listener {
    @EventHandler
    fun onEpisodesRelease(event: EpisodesReleaseEvent) {
        val animes =
            event.episodes.map { it.anime }.distinctBy { it?.uuid }.mapNotNull { it?.name }.sortedBy { it.lowercase() }
        println("Sending notification for ${animes.size} animes: ${animes.joinToString(", ")}")
        Notifications.send(body = animes.joinToString(", "))
    }
}
