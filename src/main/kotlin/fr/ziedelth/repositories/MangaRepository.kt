package fr.ziedelth.repositories

import fr.ziedelth.entities.Manga
import org.hibernate.Session
import java.util.*

class MangaRepository(session: Session) : AbstractRepository<Manga>(session), IPageRepository<Manga> {
    fun getByEAN(tag: String, ean: Long): Manga? {
        val query = session.createQuery("FROM Manga WHERE anime.country.tag = :tag AND ean = :ean", Manga::class.java)
        query.maxResults = 1
        query.setParameter("tag", tag)
        query.setParameter("ean", ean)
        return query.uniqueResult()
    }

    override fun getByPage(tag: String, page: Int, limit: Int): List<Manga> {
        return super.getByPage(
            page,
            limit,
            "FROM Manga WHERE anime.country.tag = :tag AND ean IS NOT NULL ORDER BY releaseDate DESC, anime.name",
            "tag" to tag,
        )
    }

    override fun getByPageWithAnime(uuid: UUID, page: Int, limit: Int): List<Manga> {
        return super.getByPage(
            page,
            limit,
            "FROM Manga WHERE anime.uuid = :uuid ORDER BY releaseDate DESC, anime.name",
            "uuid" to uuid,
        )
    }

    override fun getByPageWithList(list: List<UUID>, page: Int, limit: Int): List<Manga> {
        return super.getByPage(
            page,
            limit,
            "FROM Manga WHERE uuid IN :list ORDER BY releaseDate DESC, anime.name",
            "list" to list
        )
    }
}