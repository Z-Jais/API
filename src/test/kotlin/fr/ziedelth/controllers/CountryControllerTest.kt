package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Country
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

internal class CountryControllerTest : AbstractAPITest() {
    @Test
    fun getAll() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.get("/countries")
            expect(HttpStatusCode.OK) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Array<Country>::class.java)
            expect(2) { json.size }
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

            val response = client.post("/countries") {
                contentType(ContentType.Application.Json)
                setBody(Country(tag = "us", name = "United States"))
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Country::class.java)
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
                client.post("/countries") {
                    contentType(ContentType.Application.Json)
                    setBody(Country(name = "France"))
                }.status
            }

            expect(HttpStatusCode.Conflict) {
                client.post("/countries") {
                    contentType(ContentType.Application.Json)
                    setBody(Country(tag = "fr", name = "Test"))
                }.status
            }

            expect(HttpStatusCode.Conflict) {
                client.post("/countries") {
                    contentType(ContentType.Application.Json)
                    setBody(Country(tag = "test", name = "France"))
                }.status
            }
        }
    }
}