package fr.ziedelth.caches

import java.util.*

data class PaginationAnimeCacheKey(
    val page: Int,
    val limit: Int,
    override val anime: UUID,
) : AnimeCacheKey(anime)
