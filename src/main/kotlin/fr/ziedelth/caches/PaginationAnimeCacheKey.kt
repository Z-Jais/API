package fr.ziedelth.caches

import fr.ziedelth.utils.SortType
import java.util.*

data class PaginationAnimeCacheKey(
    override val anime: UUID,
    val sortType: SortType,
    val page: Int,
    val limit: Int,
) : AnimeCacheKey(anime)
