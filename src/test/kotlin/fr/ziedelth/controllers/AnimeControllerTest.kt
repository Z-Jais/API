package fr.ziedelth.controllers

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Country
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

internal class AnimeControllerTest : AbstractAPITest() {
    @Test
    fun searchByHash() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.get("/animes/country/fr/search/hash/hello")
            val json = Gson().fromJson(response.bodyAsText(), JsonObject::class.java)
            val uuid = json.get("uuid").asString

            expect(HttpStatusCode.OK) { response.status }
            checkNotNull(uuid)

            expect(HttpStatusCode.NotFound) { client.get("/animes/country/fr/search/hash/azertyuiop").status }
        }
    }

    @Test
    fun searchByName() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.get("/animes/country/fr/search/name/Naruto")
            val json = Gson().fromJson(response.bodyAsText(), Array<Anime>::class.java)

            expect(HttpStatusCode.OK) { response.status }
            expect(1) { json.size }
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
            val simulcast = simulcastRepository.getAll().first()

            // NOT CACHED

            val responseNotCached =
                client.get("/animes/country/${country.tag}/simulcast/${simulcast.uuid}/page/1/limit/12")
            val jsonNotCached = Gson().fromJson(responseNotCached.bodyAsText(), Array<Anime>::class.java)

            expect(HttpStatusCode.OK) { responseNotCached.status }
            expect(3) { jsonNotCached.size }

            // CACHED

            val responseCached =
                client.get("/animes/country/${country.tag}/simulcast/${simulcast.uuid}/page/1/limit/12")
            val jsonCached = Gson().fromJson(responseCached.bodyAsText(), Array<Anime>::class.java)

            expect(HttpStatusCode.OK) { responseCached.status }
            expect(3) { jsonCached.size }
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
            val simulcast = simulcastRepository.getAll().first()

            // ERROR

            val responseError =
                client.get("/animes/country/${country.tag}/simulcast/${simulcast.uuid}/page/ae/limit/12")

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

            val response = client.post("/animes/watchlist/page/1/limit/12") {
                setBody(bodyRequest)
            }

            val json = Gson().fromJson(response.bodyAsText(), Array<Anime>::class.java)

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

            val anime = animeRepository.getAll().first()
            val bodyRequest = Encoder.toGzip("[\"${anime.uuid}\"]")

            val responseError = client.post("/animes/watchlist/page/ae/limit/12") {
                setBody(bodyRequest)
            }

            expect(HttpStatusCode.InternalServerError) { responseError.status }
        }
    }

    @Test
    fun create() {
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

            val country = countryRepository.getAll().first()

            val response = client.post("/animes") {
                contentType(ContentType.Application.Json)
                setBody(Anime(country = country, name = "Test", description = "Test", image = "Test"))
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Anime::class.java)
            checkNotNull(json.uuid)
        }
    }

    @Test
    fun createError() {
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

            val country = countryRepository.getAll().first()

            expect(HttpStatusCode.BadRequest) {
                client.post("/animes") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        Anime(
                            country = Country(tag = "us", name = "United States"),
                            name = "Test",
                            description = "Test",
                            image = "Test"
                        )
                    )
                }.status
            }

            expect(HttpStatusCode.BadRequest) {
                client.post("/animes") {
                    contentType(ContentType.Application.Json)
                    setBody(Anime(country = country, description = "Test", image = "Test"))
                }.status
            }

            expect(HttpStatusCode.Conflict) {
                client.post("/animes") {
                    contentType(ContentType.Application.Json)
                    setBody(Anime(country = country, name = "One Piece", description = "Test", image = "Test"))
                }.status
            }
        }
    }

    @Test
    fun merge() {
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

            val animes = animeRepository.getAll()
            val uuids = animes.map { it.uuid.toString() }

            val response = client.put("/animes/merge") {
                contentType(ContentType.Application.Json)
                setBody(uuids)
            }

            expect(HttpStatusCode.OK) { response.status }
            expect(1) { animeRepository.getAll().size }
        }
    }

    @Test
    fun mergeError() {
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

            expect(HttpStatusCode.NotFound) {
                client.put("/animes/merge") {
                    contentType(ContentType.Application.Json)
                    setBody(emptyList<String>())
                }.status
            }
        }
    }
}