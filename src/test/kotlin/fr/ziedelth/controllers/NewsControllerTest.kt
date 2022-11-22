package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.*
import fr.ziedelth.plugins.*
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

internal class NewsControllerTest : AbstractAPITest() {
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
                client.get("/news/country/${country.tag}/page/1/limit/12")
            val jsonNotCached = Gson().fromJson(responseNotCached.bodyAsText(), Array<News>::class.java)

            expect(HttpStatusCode.OK) { responseNotCached.status }
            expect(2) { jsonNotCached.size }

            // CACHED

            val responseCached =
                client.get("/news/country/${country.tag}/page/1/limit/12")
            val jsonCached = Gson().fromJson(responseCached.bodyAsText(), Array<News>::class.java)

            expect(HttpStatusCode.OK) { responseCached.status }
            expect(2) { jsonCached.size }
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
                client.get("/news/country/${country.tag}/page/ae/limit/12")

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
            val country = countryRepository.getAll().first()

            val response = client.post("/news/multiple") {
                contentType(ContentType.Application.Json)
                setBody(
                    listOf(
                        News(
                            country = country,
                            platform = platform,
                            title = "News 3",
                            description = "Content 3",
                            hash = "hello3",
                            url = "hello",
                        ),
                        News(
                            country = country,
                            platform = platform,
                            title = "News 4",
                            description = "Content 4",
                            hash = "hello4",
                            url = "hello",
                        ),
                    )
                )
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Array<News>::class.java)
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
                client.post("/news/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            News(
                                country = Country(),
                                platform = Platform(),
                                title = "News 3",
                                description = "Content 3",
                                hash = "hello3",
                                url = "hello",
                            ),
                        )
                    )
                }.status
            }

            val platform = platformRepository.getAll().first()

            expect(HttpStatusCode.InternalServerError) {
                client.post("/news/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            News(
                                country = Country(),
                                platform = platform,
                                title = "News 3",
                                description = "Content 3",
                                hash = "hello3",
                                url = "hello",
                            ),
                        )
                    )
                }.status
            }

            val country = countryRepository.getAll().first()

            expect(HttpStatusCode.InternalServerError) {
                client.post("/news/multiple") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            News(
                                country = country,
                                platform = platform,
                                title = "News 3",
                                description = "Content 3",
                                url = "hello",
                            ),
                        )
                    )
                }.status
            }
        }
    }
}