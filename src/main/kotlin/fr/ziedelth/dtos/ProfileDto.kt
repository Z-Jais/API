package fr.ziedelth.dtos

import java.util.*

data class ProfileDto(
    val uuid: UUID,
    val creationDate: String,
    val lastUpdate: String,
    val animes: Set<UUID>,
    val episodes: Set<UUID>,
    val totalDurationSeen: Long,
    var token: String? = null,
)
