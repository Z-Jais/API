package fr.ziedelth.controllers

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.LangType
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

internal class LangTypeControllerTest : AbstractAPITest() {
    @Test
    fun getAll() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.get("/langtypes")
            expect(HttpStatusCode.OK) { response.status }
            val json = Constant.gson.fromJson(response.bodyAsText(), Array<LangType>::class.java)
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

            val response = client.post("/langtypes") {
                contentType(ContentType.Application.Json)
                setBody(LangType(name = "test"))
            }

            expect(HttpStatusCode.Created) { response.status }
            val json = Constant.gson.fromJson(response.bodyAsText(), LangType::class.java)
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
                client.post("/langtypes") {
                    contentType(ContentType.Application.Json)
                    setBody(LangType())
                }.status
            }

            expect(HttpStatusCode.Conflict) {
                client.post("/langtypes") {
                    contentType(ContentType.Application.Json)
                    setBody(LangType(name = "SUBTITLES"))
                }.status
            }
        }
    }
}