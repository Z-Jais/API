package fr.ziedelth.events

import fr.ziedelth.entities.Episode
import fr.ziedelth.utils.plugins.events.Event

data class EpisodesReleaseEvent(val episodes: Collection<Episode>) : Event