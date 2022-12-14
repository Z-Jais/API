package fr.ziedelth.repositories

import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Country
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.utils.Database
import org.hibernate.Session

class SimulcastRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<Simulcast>(session) {
    fun getAll(tag: String?): List<Simulcast> {
        val start = System.currentTimeMillis()

        val session = getSession.invoke()
        val criteriaBuilder = session.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(Simulcast::class.java)
        val root = criteriaQuery.from(Anime::class.java)
        criteriaQuery.select(root.get("simulcasts")).distinct(true)
        criteriaQuery.where(criteriaBuilder.equal(root.get<Country>("country").get<String>("tag"), tag))
        val list = session.createQuery(criteriaQuery).list()
        session.close()

        // Sort by year and season started by "Winter", "Spring", "Summer", "Autumn"
        val seasons = listOf("WINTER", "SPRING", "SUMMER", "AUTUMN")
        val sorted = list.sortedWith(compareBy({ it.year }, { seasons.indexOf(it.season) }))

        val end = System.currentTimeMillis()
        println("SimulcastRepository.getAll() took ${end - start}ms")
        return sorted
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