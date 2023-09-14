package fr.ziedelth.caches

data class PaginationCountryCacheKey(
    val page: Int,
    val limit: Int,
    override val tag: String,
) : CountryCacheKey(tag)
