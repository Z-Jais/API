package fr.ziedelth.dtos

import fr.ziedelth.entities.Anime

data class MissingAnime(
    val anime: Anime? = null,
    val episodeCount: Long = 0,
    val lastEpisode: String? = null,
)
