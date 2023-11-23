package fr.ziedelth.events

import fr.ziedelth.dtos.RecommendedAnimeDto
import fr.ziedelth.entities.Anime
import fr.ziedelth.utils.plugins.events.Event

data class RecommendationEvent(val anime: Anime, val recommendations: List<RecommendedAnimeDto>) : Event