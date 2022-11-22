package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.EpisodeType
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

internal class EpisodeTypeControllerTest : AbstractAPITest() {
    @Test
    fun getAll() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.get("/episodetypes")
            expect(HttpStatusCode.OK) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Array<EpisodeType>::class.java)
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

            val response = client.post("/episodetypes") {
                contentType(ContentType.Application.Json)
                setBody(EpisodeType(name = "test"))
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), EpisodeType::class.java)
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
                client.post("/episodetypes") {
                    contentType(ContentType.Application.Json)
                    setBody(EpisodeType())
                }.status
            }

            expect(HttpStatusCode.Conflict) {
                client.post("/episodetypes") {
                    contentType(ContentType.Application.Json)
                    setBody(EpisodeType(name = "Episode"))
                }.status
            }
        }
    }
}