package fr.ziedelth.controllers

import com.google.gson.JsonObject
import fr.ziedelth.AbstractAPITest
import fr.ziedelth.plugins.configureHTTP
import fr.ziedelth.plugins.configureRoutingTest
import fr.ziedelth.utils.Constant
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertNotSame
import kotlin.test.expect

internal class ProfileControllerTest : AbstractAPITest() {
    @Test
    fun getTotalDuration() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/total-duration") { setBody(getFilterDataEncoded()) }

            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val totalDuration = json.getAsJsonPrimitive("total-duration").asInt

            assertNotSame(0, totalDuration)
            expect(1440) { totalDuration }
        }
    }
}