package fr.ziedelth.caches

data class DayCountryCacheKey(
    override val tag: String,
    val day: Int,
) : CountryCacheKey(tag)
