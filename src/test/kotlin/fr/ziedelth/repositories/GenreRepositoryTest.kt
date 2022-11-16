package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Genre
import fr.ziedelth.plugins.genreRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.expect

internal class GenreRepositoryTest : AbstractAPITest() {
    @Test
    fun find() {
        val genre = genreRepository.find(UUID.randomUUID())
        expect(null) { genre }

        val genres = genreRepository.getAll()
        expect(genres.first().uuid) { genreRepository.find(genres.first().uuid)?.uuid }
    }

    @Test
    fun exists() {
        expect(true) { genreRepository.exists("name", "Action") }
        expect(false) { genreRepository.exists("uuid", UUID.randomUUID()) }
    }

    @Test
    fun findAll() {
        expect(emptyList()) { genreRepository.findAll(listOf(UUID.randomUUID())) }

        val genres = genreRepository.getAll()
        val list = genreRepository.findAll(listOf(genres[0].uuid, genres[1].uuid))

        expect(true) { list.any { it.uuid == genres.first().uuid } && list.any { it.uuid == genres[1].uuid } }
    }

    @Test
    fun getAll() {
        expect(3) { genreRepository.getAll().size }
    }

    @Test
    fun save() {
        assertThrows<Exception> {
            genreRepository.save(Genre(name = "Action"))
        }

        val genre = Genre(name = "Fantasy")
        genreRepository.save(genre)

        expect(4) { genreRepository.getAll().size }
        checkNotNull { genre.uuid }
        expect("Fantasy") { genre.name }
    }

    @Test
    fun saveAll() {
        assertThrows<Exception> {
            genreRepository.saveAll(
                listOf(
                    Genre(name = "Action"),
                    Genre(name = "Drama")
                )
            )
        }

        val genre1 = Genre(name = "Fantasy")
        val genre2 = Genre(name = "Horror")
        genreRepository.saveAll(listOf(genre1, genre2))

        expect(5) { genreRepository.getAll().size }
        checkNotNull { genre1.uuid }
        checkNotNull { genre2.uuid }
        expect("Fantasy") { genre1.name }
        expect("Horror") { genre2.name }
    }

    @Test
    fun delete() {
        assertThrows<Exception> {
            genreRepository.delete(
                Genre(
                    name = "Action"
                )
            )
        }

        val genres = genreRepository.getAll()
        genreRepository.delete(genres.first())
        expect(2) { genres.size - 1 }
    }
}
