package fr.ziedelth.repositories

import fr.ziedelth.entities.Anime
import fr.ziedelth.utils.Database
import org.hibernate.Session
import java.util.*

class AnimeRepository(session: () -> Session = { Database.getSession() }) : IRepository<Anime>(session) {
    fun findByHash(tag: String, hash: String): UUID? {
        val session = getSession.invoke()
        val query = session.createQuery(
            "SELECT a.uuid FROM Anime a JOIN a.hashes h WHERE a.country.tag = :tag AND h = :hash",
            UUID::class.java
        )
        query.maxResults = 1
        query.setParameter("tag", tag)
        query.setParameter("hash", hash)
        val uuid = query.uniqueResult()
        session.close()
        return uuid
    }

    fun findOneByName(tag: String, name: String): Anime? {
        val session = getSession.invoke()
        val query = session.createQuery(
            "FROM Anime WHERE country.tag = :tag AND LOWER(name) = :name",
            Anime::class.java
        )
        query.maxResults = 1
        query.setParameter("tag", tag)
        query.setParameter("name", name.lowercase())
        val uuid = query.uniqueResult()
        session.close()
        return uuid
    }

    fun findByName(tag: String, name: String): List<Anime> {
        val session = getSession.invoke()
        val query = session.createQuery(
            "SELECT DISTINCT anime FROM Episode e WHERE e.anime.country.tag = :tag AND LOWER(e.anime.name) LIKE CONCAT('%', :name, '%') ORDER BY e.anime.name",
            Anime::class.java
        )
        query.setParameter("tag", tag)
        query.setParameter("name", name.lowercase())
        val list = query.list()
        session.close()
        return list
    }

    fun getByPage(tag: String, simulcast: UUID, page: Int, limit: Int): List<Anime> {
        val session = getSession.invoke()
        val query = session.createQuery(
            "FROM Anime a JOIN a.simulcasts s WHERE a.country.tag = :tag AND s.uuid = :simulcast ORDER BY a.name",
            Anime::class.java
        )
        query.setParameter("tag", tag)
        query.setParameter("simulcast", simulcast)
        query.firstResult = (limit * page) - limit
        query.maxResults = limit
        val list = query.list()
        session.close()
        return list
    }

    fun findAllByPage(uuids: List<UUID>, page: Int, limit: Int): List<Anime> {
        val session = getSession.invoke()
        val query = session.createQuery("FROM Anime WHERE uuid IN :list ORDER BY name", Anime::class.java)
        query.setParameter("list", uuids)
        query.firstResult = (limit * page) - limit
        query.maxResults = limit
        val list = query.list()
        session.close()
        return list
    }
}