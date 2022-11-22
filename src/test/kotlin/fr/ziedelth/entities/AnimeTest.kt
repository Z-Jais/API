package fr.ziedelth.entities

import org.junit.jupiter.api.Test
import kotlin.test.expect

class AnimeTest {
    @Test
    fun hash() {
        val anime = Anime(
            name = "Do It Yourself!!",
            image = "hello",
            country = Country(tag = "fr", name = "France")
        )

        expect("do-it-yourself") { anime.hash() }
    }
}