package fr.ziedelth.caches

open class CountryCacheKey(
    open val tag: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CountryCacheKey) return false

        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
}
