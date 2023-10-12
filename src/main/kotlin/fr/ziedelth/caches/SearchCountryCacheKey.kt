package fr.ziedelth.caches

data class SearchCountryCacheKey(
    override val tag: String,
    val search: String,
) : CountryCacheKey(tag)
