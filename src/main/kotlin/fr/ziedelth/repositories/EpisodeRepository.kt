package fr.ziedelth.repositories

import fr.ziedelth.entities.Episode
import fr.ziedelth.utils.Database
import org.hibernate.Session
import java.util.UUID

class EpisodeRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<Episode>(session) {
    fun getByPage(tag: String, page: Int, limit: Int): List<Episode> {
        val session = getSession.invoke()
        val query = session.createQuery(
            "FROM Episode WHERE anime.country.tag = :tag ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name",
            Episode::class.java
        )
        query.setParameter("tag", tag)
        query.firstResult = (limit * page) - limit
        query.maxResults = limit
        val list = query.list()
        session.close()
        return list
    }

    fun getByPageWithAnime(uuid: UUID, page: Int, limit: Int): List<Episode> {
        val session = getSession.invoke()
        val query = session.createQuery(
            "FROM Episode WHERE anime.uuid = :uuid ORDER BY season DESC, number DESC, episodeType.name, langType.name",
            Episode::class.java
        )
        query.setParameter("uuid", uuid)
        query.firstResult = (limit * page) - limit
        query.maxResults = limit
        val list = query.list()
        session.close()
        return list
    }

    fun getByPageWithList(list: List<UUID>, page: Int, limit: Int): List<Episode> {
        val session = getSession.invoke()
        val query = session.createQuery(
            "FROM Episode WHERE anime.uuid IN :list ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name",
            Episode::class.java
        )
        query.setParameter("list", list)
        query.firstResult = (limit * page) - limit
        query.maxResults = limit
        val result = query.list()
        session.close()
        return result
    }
}