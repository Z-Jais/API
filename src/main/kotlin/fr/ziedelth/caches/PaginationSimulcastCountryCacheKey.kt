package fr.ziedelth.caches

import java.util.*

data class PaginationSimulcastCountryCacheKey(
    override val tag: String,
    val simulcast: UUID,
    val page: Int,
    val limit: Int,
) : CountryCacheKey(tag)
