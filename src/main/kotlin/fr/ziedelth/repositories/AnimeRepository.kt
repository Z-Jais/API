package fr.ziedelth.repositories

import fr.ziedelth.entities.Anime
import org.hibernate.Session
import java.util.*

class AnimeRepository(session: Session) : AbstractRepository<Anime>(session), IPageRepository<Anime> {
    fun findByHash(tag: String, hash: String): UUID? {
        val query = session.createQuery(
            "SELECT a.uuid FROM Anime a JOIN a.hashes h WHERE a.country.tag = :tag AND h = :hash",
            UUID::class.java
        )
        query.maxResults = 1
        query.setParameter("tag", tag)
        query.setParameter("hash", hash)
        return query.uniqueResult()
    }

    fun findOneByName(tag: String, name: String): Anime? {
        val query = session.createQuery(
            "FROM Anime WHERE country.tag = :tag AND LOWER(name) = :name",
            Anime::class.java
        )
        query.maxResults = 1
        query.setParameter("tag", tag)
        query.setParameter("name", name.lowercase())
        return query.uniqueResult()
    }

    fun findByName(tag: String, name: String): List<Anime> {
        val query = session.createQuery(
            "SELECT DISTINCT anime FROM Episode e WHERE e.anime.country.tag = :tag AND LOWER(e.anime.name) LIKE CONCAT('%', :name, '%') ORDER BY e.anime.name",
            Anime::class.java
        )
        query.setParameter("tag", tag)
        query.setParameter("name", name.lowercase())
        return query.list()
    }

    fun getByPage(tag: String, simulcast: UUID, page: Int, limit: Int): List<Anime> {
        return super.getByPage(
            page,
            limit,
            "FROM Anime a JOIN a.simulcasts s WHERE a.country.tag = :tag AND s.uuid = :simulcast ORDER BY a.name",
            "tag" to tag,
            "simulcast" to simulcast
        )
    }

    override fun getByPageWithList(list: List<UUID>, page: Int, limit: Int): List<Anime> {
        return super.getByPage(
            page,
            limit,
            "FROM Anime WHERE uuid IN :list ORDER BY name",
            "list" to list
        )
    }

    fun getDiary(tag: String, day: Int): List<Anime> {
        val query = session.createQuery(
            "SELECT DISTINCT anime FROM Episode episode WHERE episode.anime.country.tag = :tag AND current_date - to_date(episode.releaseDate, 'YYYY-MM-DDTHH:MI:SS') <= 7 AND FUNCTION('date_part', 'dow', to_date(episode.releaseDate, 'YYYY-MM-DDTHH:MI:SS')) = :day ORDER BY episode.anime.name ASC",
            Anime::class.java
        )
        query.setParameter("tag", tag)
        query.setParameter("day", day)
        return query.list() ?: emptyList()
    }

    override fun getByPage(tag: String, page: Int, limit: Int): List<Anime> {
        TODO("Not yet implemented")
    }

    override fun getByPageWithAnime(uuid: UUID, page: Int, limit: Int): List<Anime> {
        TODO("Not yet implemented")
    }
}