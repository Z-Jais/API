package fr.ziedelth.caches

data class SearchCountryCacheKey(
    val search: String,
    override val tag: String,
) : CountryCacheKey(tag)
