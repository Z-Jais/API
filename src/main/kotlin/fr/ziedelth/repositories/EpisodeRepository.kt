package fr.ziedelth.repositories

import fr.ziedelth.entities.Episode
import org.hibernate.Session
import org.hibernate.jpa.AvailableHints
import java.util.*

class EpisodeRepository(session: Session) : AbstractRepository<Episode>(session), IPageRepository<Episode> {
    override fun getByPage(tag: String, page: Int, limit: Int): List<Episode> {
        return super.getByPage(
            page,
            limit,
            "FROM Episode WHERE anime.country.tag = :tag ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name",
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
            "FROM Episode WHERE anime.uuid IN :list ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name",
            "list" to list
        )
    }

    fun getLastNumber(episode: Episode): Int {
        val query = session.createQuery(
            "SELECT number FROM Episode WHERE anime.uuid = :uuid AND platform = :platform AND season = :season AND episodeType.uuid = :episodeType AND langType.uuid = :langType ORDER BY number DESC",
            Int::class.java
        )
        query.maxResults = 1
        query.setParameter("uuid", episode.anime?.uuid)
        query.setParameter("platform", episode.platform)
        query.setParameter("season", episode.season)
        query.setParameter("episodeType", episode.episodeType?.uuid)
        query.setParameter("langType", episode.langType?.uuid)
        query.setHint(AvailableHints.HINT_READ_ONLY, true)
        return query.uniqueResult() ?: 0
    }
}