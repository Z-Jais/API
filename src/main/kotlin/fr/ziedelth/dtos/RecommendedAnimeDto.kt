package fr.ziedelth.dtos

import fr.ziedelth.entities.Anime

data class RecommendedAnimeDto(
    val anime: Anime,
    val score: Double,
)
