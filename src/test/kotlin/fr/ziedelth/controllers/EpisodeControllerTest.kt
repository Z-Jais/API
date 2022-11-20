package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.*
import fr.ziedelth.plugins.*
import fr.ziedelth.utils.Encoder
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
            val jsonNotCached = Gson().fromJson(responseNotCached.bodyAsText(), Array<Episode>::class.java)

            expect(HttpStatusCode.OK) { responseNotCached.status }
            expect(12) { jsonNotCached.size }

            // CACHED

            val responseCached =
                client.get("/episodes/country/${country.tag}/page/1/limit/12")
            val jsonCached = Gson().fromJson(responseCached.bodyAsText(), Array<Episode>::class.java)

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

            val responseError =
                client.get("/episodes/country/${country.tag}/page/ae/limit/12")

            expect(HttpStatusCode.InternalServerError) { responseError.status }
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
            val json = Gson().fromJson(response.bodyAsText(), Array<Episode>::class.java)

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
    fun getWatchlistByPage() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val anime = animeRepository.getAll().first()
            val bodyRequest = Encoder.toGzip("[\"${anime.uuid}\"]")

            val response = client.post("/episodes/watchlist/page/1/limit/12") {
                setBody(bodyRequest)
            }

            val json = Gson().fromJson(response.bodyAsText(), Array<Episode>::class.java)

            expect(HttpStatusCode.OK) { response.status }
            expect(10) { json.size }
        }
    }

    @Test
    fun getWatchlistByPageError() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val anime = animeRepository.getAll().first()
            val bodyRequest = Encoder.toGzip("[\"${anime.uuid}\"]")

            val responseError = client.post("/episodes/watchlist/page/ae/limit/12") {
                setBody(bodyRequest)
            }

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
            val anime = animeRepository.getAll().first()
            val episodeType = episodeTypeRepository.getAll().first()
            val langType = langTypeRepository.getAll().first()

            val response = client.post("/episodes/multiple") {
                contentType(ContentType.Application.Json)
                setBody(listOf(
                    Episode(
                        anime = anime,
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
                        anime = anime,
                        platform = platform,
                        episodeType = episodeType,
                        langType = langType,
                        number = 2,
                        season = 1,
                        url = "https://www.google.com",
                        image = "https://www.google.com",
                        hash = "azertyuiop",
                    )
                ))
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Array<Episode>::class.java)
            expect(2) { json.size }
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