package fr.ziedelth.caches

data class DayCountryCacheKey(
    val day: Int,
    override val tag: String,
) : CountryCacheKey(tag)
