package fr.ziedelth.listeners

import fr.ziedelth.entities.Episode
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.utils.Notifications
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener

class EpisodesRelease : Listener {
    private fun toString(episode: Episode): String {
        val etName = when (episode.episodeType!!.name) {
            "EPISODE" -> "Épisode"
            "SPECIAL" -> "Spécial"
            "FILM" -> "Film"
            else -> "Épisode"
        }

        val ltName = when (episode.langType!!.name) {
            "SUBTITLES" -> "VOSTFR"
            "VOICE" -> "VF"
            else -> "VOSTFR"
        }

        return "Saison ${episode.season} • $etName ${episode.number} $ltName"
    }

    @EventHandler
    fun onEpisodesRelease(event: EpisodesReleaseEvent) {
        event.episodes.forEach {
            Notifications.send(it.anime!!.name!!, toString(it), it.image!!)
            Notifications.send(it.anime!!.name!!, toString(it), it.image, it.anime!!.uuid.toString())
        }
    }
}
