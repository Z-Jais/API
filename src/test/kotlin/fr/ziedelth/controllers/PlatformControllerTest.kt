package fr.ziedelth.controllers

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Platform
import fr.ziedelth.plugins.*
import fr.ziedelth.utils.Constant
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

internal class PlatformControllerTest : AbstractAPITest() {
    @Test
    fun getAll() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.get("/platforms")
            expect(HttpStatusCode.OK) { response.status }
            val json = Constant.gson.fromJson(response.bodyAsText(), Array<Platform>::class.java)
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

            val response = client.post("/platforms") {
                contentType(ContentType.Application.Json)
                setBody(Platform(name = "MangaNews", url = "hello", image = "hello"))
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Constant.gson.fromJson(response.bodyAsText(), Platform::class.java)
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
                client.post("/platforms") {
                    contentType(ContentType.Application.Json)
                    setBody(Platform(name = "MangaNews"))
                }.status
            }

            expect(HttpStatusCode.BadRequest) {
                client.post("/platforms") {
                    contentType(ContentType.Application.Json)
                    setBody(Platform(url = "hello"))
                }.status
            }

            expect(HttpStatusCode.BadRequest) {
                client.post("/platforms") {
                    contentType(ContentType.Application.Json)
                    setBody(Platform(image = "hello"))
                }.status
            }

            expect(HttpStatusCode.Conflict) {
                client.post("/platforms") {
                    contentType(ContentType.Application.Json)
                    setBody(Platform(name = "Netflix", url = "hello", image = "hello"))
                }.status
            }
        }
    }
}