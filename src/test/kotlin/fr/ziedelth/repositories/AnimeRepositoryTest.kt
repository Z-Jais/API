package fr.ziedelth.repositories

import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Country
import fr.ziedelth.utils.DatabaseTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.expect

internal class AnimeRepositoryTest {
    private val animeRepository = AnimeRepository { DatabaseTest.getSession() }

    @BeforeEach
    fun tearUp() {
        val anime = Anime(country = Country(tag = "fr", name = "France"), name = "One Piece", image = "hello")
        animeRepository.save(anime)
    }

    @AfterEach
    fun tearDown() {
        DatabaseTest.clean()
    }

    @Test
    fun getAll() {
        val animes = animeRepository.getAll()
        expect(1) { animes.size }
    }

    @Test
    fun find() {
        val anime = animeRepository.find(UUID.randomUUID())
        expect(null) { anime }
    }

    @Test
    fun save() {
        assertThrows<Exception> { animeRepository.save(Anime(country = Country(tag = "fr", name = "France"), name = "Naruto", image = "hello")) }

        val anime = Anime(country = Country(tag = "us", name = "United States"), name = "Naruto", image = "hello")
        animeRepository.save(anime)

        expect(2) { animeRepository.getAll().size }
        checkNotNull { anime.uuid }
        expect("Naruto") { anime.name }
    }

    @Test
    fun delete() {
        assertThrows<Exception> { animeRepository.delete(Anime(country = Country(tag = "fr", name = "France"), name = "Naruto", image = "hello")) }

        animeRepository.delete(animeRepository.getAll().first())
        expect(0) { animeRepository.getAll().size }
    }
}
