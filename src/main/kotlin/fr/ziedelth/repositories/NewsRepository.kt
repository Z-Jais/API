package fr.ziedelth.repositories

import fr.ziedelth.entities.News
import fr.ziedelth.utils.Database
import org.hibernate.Session

class NewsRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<News>(session) {
    fun getByPage(tag: String, page: Int, limit: Int): List<News> {
        return super.getByPage(
            page,
            limit,
            "FROM News WHERE country.tag = :tag ORDER BY releaseDate DESC",
            "tag" to tag,
        )
    }
}