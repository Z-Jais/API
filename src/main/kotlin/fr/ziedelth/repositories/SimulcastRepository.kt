package fr.ziedelth.repositories

import fr.ziedelth.entities.Simulcast
import fr.ziedelth.utils.Constant
import fr.ziedelth.utils.Database

class SimulcastRepository(database: Database) : AbstractRepository<Simulcast>(database) {
    fun getAll(tag: String?): List<Simulcast> {
        val list = database.inTransaction {
            val query = it.createQuery("SELECT simulcasts FROM Anime WHERE country.tag = :tag", Simulcast::class.java)
            query.setParameter("tag", tag)
            query.list()
        }

        return list.sortedWith(compareBy({ it.year }, { Constant.seasons.indexOf(it.season) }))
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