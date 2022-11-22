package fr.ziedelth.repositories

import fr.ziedelth.entities.Simulcast
import fr.ziedelth.utils.Database
import org.hibernate.Session

class SimulcastRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<Simulcast>(session) {
    fun getAll(tag: String?): List<Simulcast> {
        val session = getSession.invoke()
        val query = session.createQuery(
            "SELECT DISTINCT simulcasts FROM Anime WHERE country.tag = :tag",
            Simulcast::class.java
        )
        query.setParameter("tag", tag)
        val list = query.list()
        session.close()
        return list
    }

    fun findBySeasonAndYear(season: String, year: Int): Simulcast? {
        val session = getSession.invoke()
        val query = session.createQuery("FROM Simulcast WHERE season = :season AND year = :year", Simulcast::class.java)
        query.setParameter("season", season)
        query.setParameter("year", year)
        val simulcast = query.uniqueResult()
        session.close()
        return simulcast
    }
}