package fr.ziedelth.repositories

import fr.ziedelth.entities.News
import org.hibernate.Session
import java.util.*

class NewsRepository(session: Session) : AbstractRepository<News>(session), IPageRepository<News> {
    override fun getByPage(tag: String, page: Int, limit: Int): List<News> {
        return super.getByPage(
            page,
            limit,
            "FROM News WHERE country.tag = :tag ORDER BY releaseDate DESC",
            "tag" to tag,
        )
    }

    override fun getByPageWithAnime(uuid: UUID, page: Int, limit: Int): List<News> {
        TODO("Not yet implemented")
    }

    override fun getByPageWithList(list: List<UUID>, page: Int, limit: Int): List<News> {
        TODO("Not yet implemented")
    }
}