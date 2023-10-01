package fr.ziedelth.controllers

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.*
import fr.ziedelth.entities.Platform
import fr.ziedelth.plugins.*
import fr.ziedelth.utils.CalendarConverter
import fr.ziedelth.utils.Constant
import fr.ziedelth.utils.toISO8601
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.expect

internal class EpisodeControllerTest : AbstractAPITest() {
    @Test
    fun getByPage() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val country = countryRepository.getAll().first()

            // NOT CACHED

            val responseNotCached =
                client.get("/episodes/country/${country.tag}/page/1/limit/12")
            val jsonNotCached = Constant.gson.fromJson(responseNotCached.bodyAsText(), Array<Episode>::class.java)

            expect(HttpStatusCode.OK) { responseNotCached.status }
            expect(12) { jsonNotCached.size }

            // CACHED

            val responseCached =
                client.get("/episodes/country/${country.tag}/page/1/limit/12")
            val jsonCached = Constant.gson.fromJson(responseCached.bodyAsText(), Array<Episode>::class.java)

            expect(HttpStatusCode.OK) { responseCached.status }
            expect(12) { jsonCached.size }
        }
    }

    @Test
    fun getByPageError() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val country = countryRepository.getAll().first()

            // ERROR

            expect(HttpStatusCode.InternalServerError) { client.get("/episodes/country/${country.tag}/page/ae/limit/12").status }
            expect(HttpStatusCode.InternalServerError) { client.get("/episodes/country/${country.tag}/page/1/limit/ae").status }
            expect(HttpStatusCode.InternalServerError) { client.get("/episodes/country/${country.tag}/page/0/limit/12").status }
            expect(HttpStatusCode.InternalServerError) { client.get("/episodes/country/${country.tag}/page/1/limit/0").status }
            expect(HttpStatusCode.InternalServerError) { client.get("/episodes/country/${country.tag}/page/1/limit/31").status }
        }
    }

    @Test
    fun getByPageWithAnime() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val anime = animeRepository.getAll().first()

            val response =
                client.get("/episodes/anime/${anime.uuid}/page/1/limit/12")
            val json = Constant.gson.fromJson(response.bodyAsText(), Array<Episode>::class.java)

            expect(HttpStatusCode.OK) { response.status }
            expect(10) { json.size }
        }
    }

    @Test
    fun getByPageWithAnimeError() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val anime = animeRepository.getAll().first()

            // ERROR

            val responseError =
                client.get("/episodes/anime/${anime.uuid}/page/ae/limit/12")

            expect(HttpStatusCode.InternalServerError) { responseError.status }
        }
    }

    @Test
    fun save() {
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    gson()
                }
            }

            application {
                configureHTTP()
                configureRoutingTest()
            }

            val platform = platformRepository.getAll().first()
            val anime = animeRepository.getAll().last()
            val episodeType = episodeTypeRepository.getAll().last()
            val langType = langTypeRepository.getAll().first()

            val date = "2023-09-10T00:00:00Z"

            val response = client.post("/episodes/multiple") {
                contentType(ContentType.Application.Json)
                setBody(
                    listOf(
                        Episode(
                            anime = anime,
                            platform = platform,
                            releaseDate = date,
                            episodeType = episodeType,
                            langType = langType,
                            number = 1,
                            season = 1,
                            url = "https://www.google.com",
                            image = "https://www.google.com",
                            hash = "hash",
                        ),
                        Episode(
                            anime = anime,
                            platform = platform,
                            releaseDate = date,
                            episodeType = episodeType,
                            langType = langType,
                            number = 2,
                            season = 1,
                            url = "https://www.google.com",
                            image = "https://www.google.com",
                            hash = "azertyuiop",
                        ),
                        Episode(
                            anime = anime,
                            platform = platform,
                            releaseDate = date,
                            episodeType = episodeType,
                            langType = langType,
                            number = -1,
                            season = 1,
                            url = "https://www.google.com",
                            image = "https://www.google.com",
                            hash = "awzsxedcrfvtgbyhn",
                        )
                    )
                )
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Constant.gson.fromJson(response.bodyAsText(), Array<Episode>::class.java)
            expect(3) { json.size }
            expect(3) { json[2].number }
        }
    }

    @Test
    fun saveForNextSimulcast() {
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    gson()
                }
            }

            application {
                configureHTTP()
                configureRoutingTest()
            }

            val platform = platformRepository.getAll().first()
            val anime = animeRepository.getAll()
            val episodeType = episodeTypeRepository.getAll().last()
            val langType = langTypeRepository.getAll().first()

            val date = "2023-09-28T00:00:00Z"

            val response = client.post("/episodes/multiple") {
                contentType(ContentType.Application.Json)
                setBody(
                    listOf(
                        Episode(
                            anime = anime.first(),
                            releaseDate = date,
                            platform = platform,
                            episodeType = episodeType,
                            langType = langType,
                            number = 1,
                            season = 1,
                            url = "https://www.google.com",
                            image = "https://www.google.com",
                            hash = "hash",
                        ),
                        Episode(
                            anime = anime.last(),
                            releaseDate = date,
                            platform = platform,
                            episodeType = episodeType,
                            langType = langType,
                            number = 12,
                            season = 1,
                            url = "https://www.google.com",
                            image = "https://www.google.com",
                            hash = "hash-2",
                        ),
                    )
                )
            }

            val tmpSimulcast = Simulcast.getSimulcastFrom(date)
            expect("SUMMER") { tmpSimulcast.season }
            expect(2023) { tmpSimulcast.year }

            val nextDate = CalendarConverter.toUTCCalendar(date)
            nextDate.add(Calendar.DAY_OF_YEAR, 15)
            val tmpNextSimulcast = Simulcast.getSimulcastFrom(nextDate.toISO8601())
            expect("AUTUMN") { tmpNextSimulcast.season }
            expect(2023) { tmpNextSimulcast.year }

            val previousDate = CalendarConverter.toUTCCalendar(date)
            previousDate.add(Calendar.DAY_OF_YEAR, -15)
            val tmpPreviousSimulcast = Simulcast.getSimulcastFrom(nextDate.toISO8601())
            expect("AUTUMN") { tmpPreviousSimulcast.season }
            expect(2023) { tmpPreviousSimulcast.year }

            expect(HttpStatusCode.Created) { response.status }
            val json = Constant.gson.fromJson(response.bodyAsText(), Array<Episode>::class.java)
            expect(2) { json.size }

            val simulcasts = json[0].anime?.simulcasts?.toMutableList()
                ?.sortedWith(compareBy({ it.year }, { Constant.seasons.indexOf(it.season) }))
            println(simulcasts)

            expect(tmpNextSimulcast.season) { simulcasts?.last()?.season }
            expect(tmpNextSimulcast.year) { simulcasts?.last()?.year }

            val simulcasts2 = json[1].anime?.simulcasts?.toMutableList()
                ?.sortedWith(compareBy({ it.year }, { Constant.seasons.indexOf(it.season) }))
            println(simulcasts2)
            expect(tmpSimulcast.season) { simulcasts2?.last()?.season }
            expect(tmpSimulcast.year) { simulcasts2?.last()?.year }
        }
    }

    @Test
    fun saveForPreviousSimulcast() {
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    gson()
                }
            }

            application {
                configureHTTP()
                configureRoutingTest()
            }

            val platform = platformRepository.getAll().first()
            val animes = animeRepository.getAll()
            val episodeType = episodeTypeRepository.getAll().last()
            val langType = langTypeRepository.getAll().first()

            val date = "2023-10-10T00:00:00Z"

            val response = client.post("/episodes/multiple") {
                contentType(ContentType.Application.Json)
                setBody(
                    listOf(
                        Episode(
                            anime = animes.first(),
                            releaseDate = date,
                            platform = platform,
                            episodeType = episodeType,
                            langType = langType,
                            number = 1,
                            season = 1,
                            url = "https://www.google.com",
                            image = "https://www.google.com",
                            hash = "hash",
                        ),
                        Episode(
                            anime = animes.last(),
                            releaseDate = date,
                            platform = platform,
                            episodeType = episodeType,
                            langType = langType,
                            number = 12,
                            season = 1,
                            url = "https://www.google.com",
                            image = "https://www.google.com",
                            hash = "hash-2",
                        ),
                        Episode(
                            anime = animes[1],
                            releaseDate = date,
                            platform = platform,
                            episodeType = episodeType,
                            langType = langType,
                            number = 2,
                            season = 1,
                            url = "https://www.google.com",
                            image = "https://www.google.com",
                            hash = "hash-3",
                        ),
                    )
                )
            }

            val tmpSimulcast = Simulcast.getSimulcastFrom(date)
            expect("AUTUMN") { tmpSimulcast.season }
            expect(2023) { tmpSimulcast.year }

            val previousDate = CalendarConverter.toUTCCalendar(date)
            previousDate.add(Calendar.DAY_OF_YEAR, -15)
            val tmpPreviousSimulcast = Simulcast.getSimulcastFrom(previousDate.toISO8601())
            expect("SUMMER") { tmpPreviousSimulcast.season }
            expect(2023) { tmpPreviousSimulcast.year }

            expect(HttpStatusCode.Created) { response.status }
            val json = Constant.gson.fromJson(response.bodyAsText(), Array<Episode>::class.java)
            expect(3) { json.size }

            val simulcasts = json[0].anime?.simulcasts?.toMutableList()
                ?.sortedWith(compareBy({ it.year }, { Constant.seasons.indexOf(it.season) }))
            println(simulcasts)

            expect(tmpSimulcast.season) { simulcasts?.last()?.season }
            expect(tmpSimulcast.year) { simulcasts?.last()?.year }

            val simulcasts2 = json[1].anime?.simulcasts?.toMutableList()
                ?.sortedWith(compareBy({ it.year }, { Constant.seasons.indexOf(it.season) }))
            println(simulcasts2)
            expect(tmpSimulcast.season) { simulcasts2?.last()?.season }
            expect(tmpSimulcast.year) { simulcasts2?.last()?.year }

            val simulcasts3 = json[2].anime?.simulcasts?.toMutableList()
                ?.sortedWith(compareBy({ it.year }, { Constant.seasons.indexOf(it.season) }))
            println(simulcasts3)
            expect(tmpPreviousSimulcast.season) { simulcasts3?.last()?.season }
            expect(tmpPreviousSimulcast.year) { simulcasts3?.last()?.year }
        }
    }

    @Test
    fun saveError() {
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    gson()
                }
            }

            application {
                configureHTTP()
                configureRoutingTest()
            }

            expect(HttpStatusCode.InternalServerError) {
                client.post("/episodes/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            Episode(
                                platform = Platform(),
                                anime = Anime(),
                                episodeType = EpisodeType(),
                                langType = LangType(),
                                number = 1,
                                season = 1,
                                url = "https://www.google.com",
                                image = "https://www.google.com",
                            ),
                        )
                    )
                }.status
            }

            val platform = platformRepository.getAll().first()

            expect(HttpStatusCode.InternalServerError) {
                client.post("/episodes/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            Episode(
                                platform = platform,
                                anime = Anime(),
                                episodeType = EpisodeType(),
                                langType = LangType(),
                                number = 1,
                                season = 1,
                                url = "https://www.google.com",
                                image = "https://www.google.com",
                            ),
                        )
                    )
                }.status
            }

            val anime = animeRepository.getAll().first()

            expect(HttpStatusCode.InternalServerError) {
                client.post("/episodes/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            Episode(
                                platform = platform,
                                anime = anime,
                                episodeType = EpisodeType(),
                                langType = LangType(),
                                number = 1,
                                season = 1,
                                url = "https://www.google.com",
                                image = "https://www.google.com",
                            ),
                        )
                    )
                }.status
            }

            val episodeType = episodeTypeRepository.getAll().first()

            expect(HttpStatusCode.InternalServerError) {
                client.post("/episodes/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            Episode(
                                platform = platform,
                                anime = anime,
                                episodeType = episodeType,
                                langType = LangType(),
                                number = 1,
                                season = 1,
                                url = "https://www.google.com",
                                image = "https://www.google.com",
                            ),
                        )
                    )
                }.status
            }

            val langType = langTypeRepository.getAll().first()

            expect(HttpStatusCode.InternalServerError) {
                client.post("/episodes/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            Episode(
                                platform = platform,
                                anime = anime,
                                episodeType = episodeType,
                                langType = langType,
                                number = 1,
                                season = 1,
                                url = "https://www.google.com",
                                image = "https://www.google.com",
                            ),
                        )
                    )
                }.status
            }
        }
    }
}