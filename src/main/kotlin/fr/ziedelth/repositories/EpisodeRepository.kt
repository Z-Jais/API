package fr.ziedelth.repositories

import fr.ziedelth.controllers.AbstractController
import fr.ziedelth.entities.Episode
import fr.ziedelth.utils.SortType
import java.util.*

private const val ORDER =
    "ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name"

class EpisodeRepository : AbstractRepository<Episode>() {
    fun getByPage(tag: String, page: Int, limit: Int): List<Episode> {
        return super.getByPage(
            page,
            limit,
            "FROM Episode WHERE anime.country.tag = :tag $ORDER",
            "tag" to tag
        )
    }

    fun getByPageWithAnime(uuid: UUID, sortType: SortType, page: Int, limit: Int): List<Episode> {
        return super.getByPage(
            page,
            limit,
            "FROM Episode WHERE anime.uuid = :uuid ${
                when (sortType) {
                    SortType.SEASON_NUMBER -> "ORDER BY season DESC, number DESC, episodeType.name, langType.name"
                    else -> ORDER
                }
            }",
            "uuid" to uuid
        )
    }

    fun getByPageWithListFilter(
        filterData: AbstractController.FilterData,
        page: Int,
        limit: Int
    ): List<Episode> {
        if (filterData.episodes.isEmpty())
            return super.getByPage(
                page,
                limit,
                "FROM Episode WHERE anime.uuid IN :animes AND episodeType.uuid IN :episodeTypes AND langType.uuid IN :langTypes $ORDER",
                "animes" to filterData.animes,
                "episodeTypes" to filterData.episodeTypes,
                "langTypes" to filterData.langTypes
            )

        return super.getByPage(
            page,
            limit,
            "FROM Episode WHERE uuid NOT IN :episodes AND anime.uuid IN :animes AND episodeType.uuid IN :episodeTypes AND langType.uuid IN :langTypes $ORDER",
            "animes" to filterData.animes,
            "episodes" to filterData.episodes,
            "episodeTypes" to filterData.episodeTypes,
            "langTypes" to filterData.langTypes
        )
    }

    fun getLastNumber(episode: Episode): Int {
        return database.inTransaction {
            val query = it.createQuery(
                "SELECT number FROM Episode WHERE anime.uuid = :uuid AND platform = :platform AND season = :season AND episodeType.uuid = :episodeType AND langType.uuid = :langType ORDER BY number DESC",
                Int::class.java
            )
            query.maxResults = 1
            query.setParameter("uuid", episode.anime?.uuid)
            query.setParameter("platform", episode.platform)
            query.setParameter("season", episode.season)
            query.setParameter("episodeType", episode.episodeType?.uuid)
            query.setParameter("langType", episode.langType?.uuid)
            query.uniqueResult() ?: 0
        }
    }

    fun getTotalDurationSeen(episodes: List<UUID>): Long {
        return database.inTransaction {
            it.createQuery("SELECT SUM(duration) FROM Episode WHERE uuid IN :uuids AND duration > 0", Long::class.java)
                .setParameter("uuids", episodes)
                .uniqueResult() ?: 0L
        }
    }
}