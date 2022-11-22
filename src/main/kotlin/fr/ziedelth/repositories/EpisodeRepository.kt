package fr.ziedelth.repositories

import fr.ziedelth.entities.Episode
import fr.ziedelth.utils.Database
import org.hibernate.Session
import java.util.*

class EpisodeRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<Episode>(session),
    IPageRepository<Episode> {
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
}