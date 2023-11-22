package fr.ziedelth.events

import fr.ziedelth.entities.Anime
import fr.ziedelth.utils.plugins.events.Event

data class RecommendationEvent(val anime: Anime, val recommendations: Set<Pair<Anime, Double>>) : Event