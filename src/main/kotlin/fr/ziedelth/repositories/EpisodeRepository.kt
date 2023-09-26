package fr.ziedelth.repositories

import fr.ziedelth.controllers.IController
import fr.ziedelth.entities.Episode
import fr.ziedelth.utils.Database
import java.util.*

private const val ORDER =
    "ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name"

class EpisodeRepository(database: Database) : AbstractRepository<Episode>(database),
    IPageRepository<Episode> {
    override fun getByPage(tag: String, page: Int, limit: Int): List<Episode> {
        return super.getByPage(
            page,
            limit,
            "FROM Episode WHERE anime.country.tag = :tag $ORDER",
            "tag" to tag
        )
    }

    override fun getByPageWithAnime(uuid: UUID, page: Int, limit: Int): List<Episode> {
        return super.getByPage(
            page,
            limit,
            "FROM Episode WHERE anime.uuid = :uuid $ORDER",
            "uuid" to uuid
        )
    }

    override fun getByPageWithList(list: List<UUID>, page: Int, limit: Int): List<Episode> {
        return super.getByPage(
            page,
            limit,
            "FROM Episode WHERE anime.uuid IN :list $ORDER",
            "list" to list
        )
    }

    fun getByPageWithListFilter(
        filterData: IController.FilterData,
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