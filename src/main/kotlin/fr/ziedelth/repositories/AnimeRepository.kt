package fr.ziedelth.repositories

import fr.ziedelth.controllers.AbstractController
import fr.ziedelth.dtos.MissingAnimeDto
import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Episode
import fr.ziedelth.utils.unaccent
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AnimeRepository : AbstractRepository<Anime>() {
    fun findByHash(tag: String, hash: String): UUID? {
        return database.inReadOnlyTransaction {
            val query = it.createQuery(
                "SELECT a.uuid FROM Anime a JOIN a.hashes h WHERE a.country.tag = :tag AND h = :hash",
                UUID::class.java
            )
            query.maxResults = 1
            query.setParameter("tag", tag)
            query.setParameter("hash", hash)
            query.uniqueResult()
        }
    }

    fun findOneByName(tag: String, name: String): Anime? {
        return database.inReadOnlyTransaction {
            val query = it.createQuery(
                "FROM Anime WHERE country.tag = :tag AND LOWER(name) = :name",
                Anime::class.java
            )
            query.maxResults = 1
            query.setParameter("tag", tag)
            query.setParameter("name", name.lowercase())
            query.uniqueResult()
        }
    }

    // CREATE EXTENSION unaccent
    fun findByName(tag: String, name: String): List<Anime> {
        return database.inReadOnlyTransaction {
            val query = it.createQuery(
                "SELECT DISTINCT anime FROM Episode e WHERE e.anime.country.tag = :tag AND LOWER(FUNCTION('unaccent', 'unaccent', e.anime.name)) LIKE CONCAT('%', :name, '%') ORDER BY e.anime.name",
                Anime::class.java
            )
            query.setParameter("tag", tag)
            query.setParameter("name", name.unaccent().lowercase())
            query.list()
        }
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

    fun getDiary(tag: String, day: Int): List<Anime> {
        return database.inReadOnlyTransaction { session ->
            val query = session.createQuery(
                "SELECT episode FROM Episode episode WHERE episode.anime.country.tag = :tag ORDER BY episode.releaseDate DESC LIMIT 100",
                Episode::class.java
            )
            query.setParameter("tag", tag)
            val list = query.list()

            list.filter {
                OffsetDateTime.parse(
                    it.releaseDate,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                ).dayOfWeek == DayOfWeek.of(day)
            }
                .sortedBy { OffsetDateTime.parse(it.releaseDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
                .mapNotNull { it.anime }
                .distinctBy { it.uuid }
        }
    }

    fun getMissingAnimes(
        filterData: AbstractController.FilterData,
        page: Int,
        limit: Int
    ): List<MissingAnimeDto> {
        val whereString = """WHERE e.anime = a AND
                        ${if (filterData.episodes.isEmpty()) "" else "e.uuid NOT IN :episodeUuids AND"}
                        e.episodeType.uuid IN :episodeTypeUuids AND
                        e.langType.uuid IN :langTypeUuids"""

        return super.getByPage(
            MissingAnimeDto::class.java,
            page,
            limit,
            """
                SELECT new fr.ziedelth.dtos.MissingAnimeDto(
                    a, 
                    (
                        SELECT count(e) 
                        FROM Episode e 
                        $whereString
                    ), 
                    (
                        SELECT max(e.releaseDate) 
                        FROM Episode e 
                        $whereString
                    )
                )
                FROM Anime a 
                WHERE a.uuid IN (:animeUuids) 
                AND (
                    SELECT count(e)
                    FROM Episode e
                    $whereString
                ) > 0
                ORDER BY (
                    SELECT max(e.releaseDate) 
                    FROM Episode e 
                    $whereString
                ) DESC
            """.trimIndent(),
            "animeUuids" to filterData.animes,
            if (filterData.episodes.isEmpty()) null else "episodeUuids" to filterData.episodes,
            "episodeTypeUuids" to filterData.episodeTypes,
            "langTypeUuids" to filterData.langTypes,
        )
    }

    fun getInvalidAnimes(tag: String): List<Anime> {
        return database.inReadOnlyTransaction { session ->
            val query = session.createQuery(
                "SELECT anime FROM Anime anime WHERE anime.country.tag = :tag AND (anime.description LIKE '(%' OR anime.description IS NULL OR TRIM(anime.description) = '')",
                Anime::class.java
            )
            query.setParameter("tag", tag)
            query.list()
        }
    }
}