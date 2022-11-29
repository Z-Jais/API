package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Simulcast
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

internal class SimulcastControllerTest : AbstractAPITest() {
    @Test
    fun getAll() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val country = countryRepository.getAll().first()

            val response = client.get("/simulcasts/country/${country.tag}")
            expect(HttpStatusCode.OK) { response.status }
            val json = Gson().fromJson(response.bodyAsText(), Array<Simulcast>::class.java)
            expect(1) { json.size }
        }
    }
}