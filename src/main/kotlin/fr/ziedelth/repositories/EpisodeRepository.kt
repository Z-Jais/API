package fr.ziedelth.repositories

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
            "FROM Episode WHERE anime.uuid = :uuid ORDER BY season DESC, number DESC, episodeType.name, langType.name",
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
        animes: List<UUID>,
        episodes: List<UUID>,
        episodeTypes: List<UUID>,
        langTypes: List<UUID>,
        page: Int,
        limit: Int
    ): List<Episode> {
        if (episodes.isEmpty())
            return super.getByPage(
                page,
                limit,
                "FROM Episode WHERE anime.uuid IN :animes AND episodeType.uuid IN :episodeTypes AND langType.uuid IN :langTypes $ORDER",
                "animes" to animes,
                "episodeTypes" to episodeTypes,
                "langTypes" to langTypes
            )

        return super.getByPage(
            page,
            limit,
            "FROM Episode WHERE uuid NOT IN :episodes AND anime.uuid IN :animes AND episodeType.uuid IN :episodeTypes AND langType.uuid IN :langTypes $ORDER",
            "animes" to animes,
            "episodes" to episodes,
            "episodeTypes" to episodeTypes,
            "langTypes" to langTypes
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
}