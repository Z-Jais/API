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

internal class MangaControllerTest : AbstractAPITest() {
    @Test
    fun searchByEAN() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val country = countryRepository.getAll().first()
            val ean = mangaRepository.getAll().first().ean

            val responseNotCached = client.get("/mangas/country/${country.tag}/search/ean/$ean")
            val json = Gson().fromJson(responseNotCached.bodyAsText(), Manga::class.java)

            expect(HttpStatusCode.OK) { responseNotCached.status }
            checkNotNull(json.uuid)
        }
    }

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
                client.get("/mangas/country/${country.tag}/page/1/limit/12")
            val jsonNotCached = Gson().fromJson(responseNotCached.bodyAsText(), Array<Manga>::class.java)

            expect(HttpStatusCode.OK) { responseNotCached.status }
            expect(12) { jsonNotCached.size }

            // CACHED

            val responseCached =
                client.get("/mangas/country/${country.tag}/page/1/limit/12")
            val jsonCached = Gson().fromJson(responseCached.bodyAsText(), Array<Manga>::class.java)

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
                client.get("/mangas/country/${country.tag}/page/ae/limit/12")

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
                client.get("/mangas/anime/${anime.uuid}/page/1/limit/12")
            val json = Gson().fromJson(response.bodyAsText(), Array<Manga>::class.java)

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
                client.get("/mangas/anime/${anime.uuid}/page/ae/limit/12")

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

            val manga = mangaRepository.getAll().first()
            val bodyRequest = Encoder.toGzip("[\"${manga.uuid}\"]")

            val response = client.post("/mangas/watchlist/page/1/limit/12") {
                setBody(bodyRequest)
            }

            val json = Gson().fromJson(response.bodyAsText(), Array<Manga>::class.java)

            expect(HttpStatusCode.OK) { response.status }
            expect(1) { json.size }
        }
    }

    @Test
    fun getWatchlistByPageError() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val manga = mangaRepository.getAll().first()
            val bodyRequest = Encoder.toGzip("[\"${manga.uuid}\"]")

            val responseError = client.post("/mangas/watchlist/page/ae/limit/12") {
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

            val response = client.post("/mangas/multiple") {
                contentType(ContentType.Application.Json)
                setBody(
                    listOf(
                        Manga(
                            platform = platform,
                            anime = anime,
                            hash = "hash",
                            url = "test",
                            cover = "test",
                            editor = "test",
                            ref = "test",
                            ean = Math.random().toLong(),
                            age = 12,
                            price = 7.5,
                        ),
                        Manga(
                            platform = platform,
                            anime = anime,
                            hash = "hash2",
                            url = "test2",
                            cover = "test2",
                            editor = "test2",
                            ref = "test2",
                            ean = Math.random().toLong(),
                            age = 12,
                            price = 7.5,
                        )
                    )
                )
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Array<Manga>::class.java)
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
                client.post("/mangas/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            Manga(
                                platform = Platform(),
                                anime = Anime(),
                                hash = "hash2",
                                url = "test2",
                                cover = "test2",
                                editor = "test2",
                                ref = "test2",
                                ean = Math.random().toLong(),
                                age = 12,
                                price = 7.5,
                            ),
                        )
                    )
                }.status
            }

            val platform = platformRepository.getAll().first()

            expect(HttpStatusCode.InternalServerError) {
                client.post("/mangas/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            Manga(
                                platform = platform,
                                anime = Anime(),
                                hash = "hash2",
                                url = "test2",
                                cover = "test2",
                                editor = "test2",
                                ref = "test2",
                                ean = Math.random().toLong(),
                                age = 12,
                                price = 7.5,
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
                            Manga(
                                platform = platform,
                                anime = anime,
                                url = "test2",
                                cover = "test2",
                                editor = "test2",
                                ref = "test2",
                                ean = Math.random().toLong(),
                                age = 12,
                                price = 7.5,
                            ),
                        )
                    )
                }.status
            }
        }
    }
}