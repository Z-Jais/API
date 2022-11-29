package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Genre
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

internal class GenreControllerTest : AbstractAPITest() {
    @Test
    fun getAll() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.get("/genres")
            expect(HttpStatusCode.OK) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Array<Genre>::class.java)
            expect(3) { json.size }
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

            val response = client.post("/genres") {
                contentType(ContentType.Application.Json)
                setBody(Genre(name = "test"))
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Genre::class.java)
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

            expect(HttpStatusCode.BadRequest) {
                client.post("/genres") {
                    contentType(ContentType.Application.Json)
                    setBody(Genre())
                }.status
            }

            expect(HttpStatusCode.Conflict) {
                client.post("/genres") {
                    contentType(ContentType.Application.Json)
                    setBody(Genre(name = "Action"))
                }.status
            }
        }
    }
}