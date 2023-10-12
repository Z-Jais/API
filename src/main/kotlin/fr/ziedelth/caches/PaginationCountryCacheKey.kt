package fr.ziedelth.caches

data class PaginationCountryCacheKey(
    override val tag: String,
    val page: Int,
    val limit: Int,
) : CountryCacheKey(tag)
