package fr.ziedelth.caches

import java.util.*

data class ProfileEpisodeTypesLangTypesPaginationCacheKey(
    override val profile: UUID,
    val episodeTypes: List<UUID>,
    val langTypes: List<UUID>,
    override val page: Int,
    override val limit: Int,
) : ProfilePaginationCacheKey(profile, page, limit)
