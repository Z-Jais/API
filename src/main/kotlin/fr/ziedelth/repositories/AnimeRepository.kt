package fr.ziedelth.repositories

import fr.ziedelth.dtos.MissingAnime
import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Episode
import fr.ziedelth.utils.Database
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AnimeRepository(database: Database) : AbstractRepository<Anime>(database),
    IPageRepository<Anime> {
    fun findByHash(tag: String, hash: String): UUID? {
        return database.inTransaction {
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
        return database.inTransaction {
            val query = it.createQuery(
                "FROM Anime WHERE country.tag = :tag AND LOWER(name) = :name",
                Anime::class.java
            )
            query.maxResults = 1
            query.setParameter("tag", tag)
            query.setParameter("name", name.lowercase())
            database.fullInitialize(query.uniqueResult())
        }
    }

    fun findByName(tag: String, name: String): List<Anime> {
        return database.inTransaction {
            val query = it.createQuery(
                "SELECT DISTINCT anime FROM Episode e WHERE e.anime.country.tag = :tag AND LOWER(e.anime.name) LIKE CONCAT('%', :name, '%') ORDER BY e.anime.name",
                Anime::class.java
            )
            query.setParameter("tag", tag)
            query.setParameter("name", name.lowercase())
            database.fullInitialize(query.list())
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

    override fun getByPageWithList(list: List<UUID>, page: Int, limit: Int): List<Anime> {
        return super.getByPage(
            page,
            limit,
            "FROM Anime WHERE uuid IN :list ORDER BY name",
            "list" to list
        )
    }

    fun getDiary(tag: String, day: Int): List<Anime> {
        return database.inTransaction { session ->
            val query = session.createQuery(
                "SELECT episode FROM Episode episode WHERE episode.anime.country.tag = :tag ORDER BY episode.releaseDate DESC LIMIT 100",
                Episode::class.java
            )
            query.setParameter("tag", tag)
            val list = query.list()

            database.fullInitialize(
                list.filter {
                    OffsetDateTime.parse(
                        it.releaseDate,
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    ).dayOfWeek == DayOfWeek.of(day)
                }
                    .sortedBy { OffsetDateTime.parse(it.releaseDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
                    .mapNotNull { it.anime }
                    .distinctBy { it.uuid }
            )
        }
    }

    fun getMissingAnimes(
        animes: List<UUID>,
        episodes: List<UUID>,
        episodeTypes: List<UUID>,
        langTypes: List<UUID>,
        page: Int,
        limit: Int
    ): List<MissingAnime> {
        val whereString = """WHERE e.anime = a AND
                        ${if (episodes.isEmpty()) "" else "e.uuid NOT IN :episodeUuids AND"}
                        e.episodeType.uuid IN :episodeTypeUuids AND
                        e.langType.uuid IN :langTypeUuids"""

        return super.getByPage(
            MissingAnime::class.java,
            page,
            limit,
            """
                SELECT new fr.ziedelth.dtos.MissingAnime(
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
            "animeUuids" to animes,
            if (episodes.isEmpty()) null else "episodeUuids" to episodes,
            "episodeTypeUuids" to episodeTypes,
            "langTypeUuids" to langTypes,
        )
    }

    override fun getByPage(tag: String, page: Int, limit: Int): List<Anime> {
        TODO("Not yet implemented")
    }

    override fun getByPageWithAnime(uuid: UUID, page: Int, limit: Int): List<Anime> {
        TODO("Not yet implemented")
    }
}