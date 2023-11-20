package fr.ziedelth.caches

import java.util.*

open class ProfilePaginationCacheKey(
    open val profile: UUID,
    open val page: Int,
    open val limit: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfilePaginationCacheKey) return false

        if (profile != other.profile) return false
        if (page != other.page) return false
        if (limit != other.limit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = profile.hashCode()
        result = 31 * result + page
        result = 31 * result + limit
        return result
    }
}
