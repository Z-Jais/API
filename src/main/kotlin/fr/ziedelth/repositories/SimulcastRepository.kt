package fr.ziedelth.repositories

import fr.ziedelth.entities.Simulcast
import fr.ziedelth.utils.Database

class SimulcastRepository(database: Database) : AbstractRepository<Simulcast>(database) {
    fun getAll(tag: String?): List<Simulcast> {
        val start = System.currentTimeMillis()

        val list = database.inTransaction {
            val query = it.createQuery("SELECT simulcasts FROM Anime WHERE country.tag = :tag", Simulcast::class.java)
            query.setParameter("tag", tag)
            query.list()
        }

        // Sort by year and season started by "Winter", "Spring", "Summer", "Autumn"
        val seasons = listOf("WINTER", "SPRING", "SUMMER", "AUTUMN")
        val sorted = list.sortedWith(compareBy({ it.year }, { seasons.indexOf(it.season) }))

        val end = System.currentTimeMillis()
        println("SimulcastRepository.getAll() took ${end - start}ms")
        return sorted
    }

    fun findBySeasonAndYear(season: String, year: Int): Simulcast? {
        return database.inTransaction {
            val query = it.createQuery("FROM Simulcast WHERE season = :season AND year = :year", Simulcast::class.java)
            query.setParameter("season", season)
            query.setParameter("year", year)
            query.uniqueResult()
        }
    }
}