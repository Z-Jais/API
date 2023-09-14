package fr.ziedelth.caches

import java.util.*

data class PaginationSimulcastCountryCacheKey(
    val page: Int,
    val limit: Int,
    val simulcast: UUID,
    override val tag: String,
) : CountryCacheKey(tag)
