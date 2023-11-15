package fr.ziedelth.listeners

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.Message
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
        Notifications.send(event.episodes.flatMap {
            listOf(
                createEpisodeNotification(it).setTopic("all").build(),
                createEpisodeNotification(it).setTopic(it.anime!!.uuid.toString()).build(),
            )
        })
    }

    private fun createEpisodeNotification(it: Episode): Message.Builder =
        Message.builder().setAndroidConfig(
            AndroidConfig.builder().setNotification(
                AndroidNotification.builder()
                    .setTitle(it.anime!!.name!!)
                    .setBody(toString(it))
                    .setImage(it.image!!)
                    .build()
            ).build()
        )
}
