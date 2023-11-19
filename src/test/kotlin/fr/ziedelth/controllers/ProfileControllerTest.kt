package fr.ziedelth.controllers

import com.google.gson.JsonObject
import fr.ziedelth.AbstractAPITest
import fr.ziedelth.dtos.ProfileDto
import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Episode
import fr.ziedelth.plugins.*
import fr.ziedelth.utils.Constant
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import java.util.*
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

    @Test
    fun registerWithoutData() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/register") { setBody("") }
            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val tokenUuid = json.getAsJsonPrimitive("tokenUuid").asString

            assertNotSame(null, tokenUuid)
            assertNotSame("", tokenUuid)

            val profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            assertNotSame(null, profile)
            expect(profile?.tokenUuid.toString()) { tokenUuid }
        }
    }

    @Test
    fun registerWithData() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/register") { setBody(getFilterDataEncoded()) }
            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val tokenUuid = json.getAsJsonPrimitive("tokenUuid").asString

            assertNotSame(null, tokenUuid)
            assertNotSame("", tokenUuid)

            val profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            assertNotSame(null, profile)
            expect(profile?.tokenUuid.toString()) { tokenUuid }
            expect(profile?.animes?.size) { 1 }
            expect(profile?.episodes?.size) { 1 }
        }
    }

    @Test
    fun login() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/register") { setBody(getFilterDataEncoded()) }
            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val tokenUuid = json.getAsJsonPrimitive("tokenUuid").asString

            assertNotSame(null, tokenUuid)
            assertNotSame("", tokenUuid)

            val profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            assertNotSame(null, profile)
            expect(profile?.tokenUuid.toString()) { tokenUuid }
            expect(profile?.animes?.size) { 1 }
            expect(profile?.episodes?.size) { 1 }

            val loginResponse = client.post("/profile/login") { setBody(tokenUuid) }
            expect(HttpStatusCode.OK) { loginResponse.status }

            val profileDto = Constant.gson.fromJson(loginResponse.bodyAsText(), ProfileDto::class.java)

            assertNotSame(null, profileDto.token)
            assertNotSame("", profileDto.token)

            expect(1440) { profileDto.totalDurationSeen }
            expect(profile?.animes?.size) { profileDto.animes.size }
            expect(profile?.episodes?.size) { profileDto.episodes.size }
        }
    }

    @Test
    fun addAnimeToWatchlist() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/register")
            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val tokenUuid = json.getAsJsonPrimitive("tokenUuid").asString

            assertNotSame(null, tokenUuid)
            assertNotSame("", tokenUuid)

            var profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            assertNotSame(null, profile)
            expect(profile?.tokenUuid.toString()) { tokenUuid }
            expect(profile?.animes?.size) { 0 }
            expect(profile?.episodes?.size) { 0 }

            // ---

            val loginResponse = client.post("/profile/login") { setBody(tokenUuid) }
            expect(HttpStatusCode.OK) { loginResponse.status }

            val profileDto = Constant.gson.fromJson(loginResponse.bodyAsText(), ProfileDto::class.java)

            assertNotSame(null, profileDto.token)
            assertNotSame("", profileDto.token)

            expect(0) { profileDto.totalDurationSeen }
            expect(profile?.animes?.size) { profileDto.animes.size }
            expect(profile?.episodes?.size) { profileDto.episodes.size }

            // ---

            val animeToAdd = animeRepository.getAll().first()
            val addToWatchlistResponse = client.put("/profile/watchlist?anime=${animeToAdd.uuid}") {
                header("Authorization", "Bearer ${profileDto.token}")
            }
            expect(HttpStatusCode.OK) { addToWatchlistResponse.status }

            profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            expect(profile?.animes?.size) { 1 }
            expect(profile?.animes?.first()?.anime?.uuid) { animeToAdd.uuid }

            expect(profile?.episodes?.size) { 0 }
        }
    }

    @Test
    fun addEpisodeToWatchlist() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/register")
            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val tokenUuid = json.getAsJsonPrimitive("tokenUuid").asString

            assertNotSame(null, tokenUuid)
            assertNotSame("", tokenUuid)

            var profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            assertNotSame(null, profile)
            expect(profile?.tokenUuid.toString()) { tokenUuid }
            expect(profile?.animes?.size) { 0 }
            expect(profile?.episodes?.size) { 0 }

            // ---

            val loginResponse = client.post("/profile/login") { setBody(tokenUuid) }
            expect(HttpStatusCode.OK) { loginResponse.status }

            val profileDto = Constant.gson.fromJson(loginResponse.bodyAsText(), ProfileDto::class.java)

            assertNotSame(null, profileDto.token)
            assertNotSame("", profileDto.token)

            expect(0) { profileDto.totalDurationSeen }
            expect(profile?.animes?.size) { profileDto.animes.size }
            expect(profile?.episodes?.size) { profileDto.episodes.size }

            // ---

            val episodeToAdd = episodeRepository.getAll().first()
            val addToWatchlistResponse = client.put("/profile/watchlist?episode=${episodeToAdd.uuid}") {
                header("Authorization", "Bearer ${profileDto.token}")
            }
            expect(HttpStatusCode.OK) { addToWatchlistResponse.status }

            profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            expect(profile?.animes?.size) { 0 }

            expect(profile?.episodes?.size) { 1 }
            expect(profile?.episodes?.first()?.episode?.uuid) { episodeToAdd.uuid }
        }
    }

    @Test
    fun removeAnimeFromWatchlist() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/register")
            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val tokenUuid = json.getAsJsonPrimitive("tokenUuid").asString

            assertNotSame(null, tokenUuid)
            assertNotSame("", tokenUuid)

            var profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            assertNotSame(null, profile)
            expect(profile?.tokenUuid.toString()) { tokenUuid }
            expect(profile?.animes?.size) { 0 }
            expect(profile?.episodes?.size) { 0 }

            // ---

            val loginResponse = client.post("/profile/login") { setBody(tokenUuid) }
            expect(HttpStatusCode.OK) { loginResponse.status }

            val profileDto = Constant.gson.fromJson(loginResponse.bodyAsText(), ProfileDto::class.java)

            assertNotSame(null, profileDto.token)
            assertNotSame("", profileDto.token)

            expect(0) { profileDto.totalDurationSeen }
            expect(profile?.animes?.size) { profileDto.animes.size }
            expect(profile?.episodes?.size) { profileDto.episodes.size }

            // ---

            val animeToRemove = animeRepository.getAll().first()
            val addToWatchlistResponse = client.put("/profile/watchlist?anime=${animeToRemove.uuid}") {
                header("Authorization", "Bearer ${profileDto.token}")
            }
            expect(HttpStatusCode.OK) { addToWatchlistResponse.status }

            profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            expect(profile?.animes?.size) { 1 }
            expect(profile?.animes?.first()?.anime?.uuid) { animeToRemove.uuid }

            expect(profile?.episodes?.size) { 0 }

            // ---

            val removeFromWatchlistResponse = client.delete("/profile/watchlist?anime=${animeToRemove.uuid}") {
                header("Authorization", "Bearer ${profileDto.token}")
            }
            expect(HttpStatusCode.OK) { removeFromWatchlistResponse.status }

            profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            expect(profile?.animes?.size) { 0 }
            expect(profile?.episodes?.size) { 0 }
        }
    }

    @Test
    fun removeEpisodeFromWatchlist() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/register")
            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val tokenUuid = json.getAsJsonPrimitive("tokenUuid").asString

            assertNotSame(null, tokenUuid)
            assertNotSame("", tokenUuid)

            var profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            assertNotSame(null, profile)
            expect(profile?.tokenUuid.toString()) { tokenUuid }
            expect(profile?.animes?.size) { 0 }
            expect(profile?.episodes?.size) { 0 }

            // ---

            val loginResponse = client.post("/profile/login") { setBody(tokenUuid) }
            expect(HttpStatusCode.OK) { loginResponse.status }

            val profileDto = Constant.gson.fromJson(loginResponse.bodyAsText(), ProfileDto::class.java)

            assertNotSame(null, profileDto.token)
            assertNotSame("", profileDto.token)

            expect(0) { profileDto.totalDurationSeen }
            expect(profile?.animes?.size) { profileDto.animes.size }
            expect(profile?.episodes?.size) { profileDto.episodes.size }

            // ---

            val episodeToRemove = episodeRepository.getAll().first()
            val addToWatchlistResponse = client.put("/profile/watchlist?episode=${episodeToRemove.uuid}") {
                header("Authorization", "Bearer ${profileDto.token}")
            }
            expect(HttpStatusCode.OK) { addToWatchlistResponse.status }

            profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            expect(profile?.animes?.size) { 0 }

            expect(profile?.episodes?.size) { 1 }
            expect(profile?.episodes?.first()?.episode?.uuid) { episodeToRemove.uuid }

            // ---

            val removeFromWatchlistResponse = client.delete("/profile/watchlist?episode=${episodeToRemove.uuid}") {
                header("Authorization", "Bearer ${profileDto.token}")
            }
            expect(HttpStatusCode.OK) { removeFromWatchlistResponse.status }

            profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            expect(profile?.animes?.size) { 0 }
            expect(profile?.episodes?.size) { 0 }
        }
    }

    @Test
    fun getWatchlistAnimes() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/register") { setBody(getFilterDataEncoded()) }
            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val tokenUuid = json.getAsJsonPrimitive("tokenUuid").asString

            assertNotSame(null, tokenUuid)
            assertNotSame("", tokenUuid)

            val profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            assertNotSame(null, profile)
            expect(profile?.tokenUuid.toString()) { tokenUuid }
            expect(profile?.animes?.size) { 1 }
            expect(profile?.episodes?.size) { 1 }

            // ---

            val loginResponse = client.post("/profile/login") { setBody(tokenUuid) }
            expect(HttpStatusCode.OK) { loginResponse.status }

            val profileDto = Constant.gson.fromJson(loginResponse.bodyAsText(), ProfileDto::class.java)

            assertNotSame(null, profileDto.token)
            assertNotSame("", profileDto.token)

            expect(1440) { profileDto.totalDurationSeen }
            expect(profile?.animes?.size) { profileDto.animes.size }
            expect(profile?.episodes?.size) { profileDto.episodes.size }

            // ---

            val getWatchlistAnimesResponse = client.get("/profile/watchlist/animes/page/1/limit/12") {
                header("Authorization", "Bearer ${profileDto.token}")
            }
            expect(HttpStatusCode.OK) { getWatchlistAnimesResponse.status }

            val watchlistAnimes = Constant.gson.fromJson(getWatchlistAnimesResponse.bodyAsText(), Array<Anime>::class.java)

            expect(1) { watchlistAnimes.size }
            expect(profile?.animes?.first()?.anime?.uuid) { watchlistAnimes.first().uuid }
        }
    }

    @Test
    fun getWatchlistEpisodes() {
        testApplication {
            application {
                configureHTTP()
                configureRoutingTest()
            }

            val response = client.post("/profile/register") { setBody(getFilterDataEncoded()) }
            expect(HttpStatusCode.OK) { response.status }

            val json = Constant.gson.fromJson(response.bodyAsText(), JsonObject::class.java)
            val tokenUuid = json.getAsJsonPrimitive("tokenUuid").asString

            assertNotSame(null, tokenUuid)
            assertNotSame("", tokenUuid)

            val profile = profileRepository.findByToken(UUID.fromString(tokenUuid))

            assertNotSame(null, profile)
            expect(profile?.tokenUuid.toString()) { tokenUuid }
            expect(profile?.animes?.size) { 1 }
            expect(profile?.episodes?.size) { 1 }

            // ---

            val loginResponse = client.post("/profile/login") { setBody(tokenUuid) }
            expect(HttpStatusCode.OK) { loginResponse.status }

            val profileDto = Constant.gson.fromJson(loginResponse.bodyAsText(), ProfileDto::class.java)

            assertNotSame(null, profileDto.token)
            assertNotSame("", profileDto.token)

            expect(1440) { profileDto.totalDurationSeen }
            expect(profile?.animes?.size) { profileDto.animes.size }
            expect(profile?.episodes?.size) { profileDto.episodes.size }

            // ---

            val getWatchlistEpisodesResponse = client.get("/profile/watchlist/episodes/page/1/limit/12") {
                header("Authorization", "Bearer ${profileDto.token}")
            }
            expect(HttpStatusCode.OK) { getWatchlistEpisodesResponse.status }

            val watchlistEpisodes = Constant.gson.fromJson(getWatchlistEpisodesResponse.bodyAsText(), Array<Episode>::class.java)

            expect(1) { watchlistEpisodes.size }
            expect(profile?.episodes?.first()?.episode?.uuid) { watchlistEpisodes.first().uuid }
        }
    }
}