package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Country
import fr.ziedelth.plugins.animeRepository
import fr.ziedelth.plugins.countryRepository
import fr.ziedelth.plugins.simulcastRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.expect

internal class AnimeRepositoryTest : AbstractAPITest() {
    @Test
    fun find() {
        val anime = animeRepository.find(UUID.randomUUID())
        expect(null) { anime }

        val animes = animeRepository.getAll()
        expect(animes.first().uuid) { animeRepository.find(animes.first().uuid)?.uuid }
    }

    @Test
    fun exists() {
        expect(true) { animeRepository.exists("name", "One Piece") }
        expect(false) { animeRepository.exists("uuid", UUID.randomUUID()) }
    }

    @Test
    fun findAll() {
        expect(emptyList()) { animeRepository.findAll(listOf(UUID.randomUUID())) }

        val animes = animeRepository.getAll()
        val list = animeRepository.findAll(listOf(animes.first().uuid, animes[1].uuid))

        expect(true) { list.any { it.uuid == animes.first().uuid } && list.any { it.uuid == animes[1].uuid } }
        expect(false) { list.any { it.uuid == animes[2].uuid } }
    }

    @Test
    fun getAll() {
        expect(3) { animeRepository.getAll().size }
    }

    @Test
    fun getAllByCountry() {
        val country = countryRepository.getAll().first()
        expect(3) { animeRepository.getAllBy("country.tag", country.tag).size }
        expect(0) { animeRepository.getAllBy("country.tag", "us").size }
    }

    @Test
    fun save() {
        assertThrows<Exception> {
            animeRepository.save(
                Anime(
                    country = Country(tag = "fr", name = "France"),
                    name = "Naruto",
                    image = "hello"
                )
            )
        }

        val anime = Anime(country = Country(tag = "us", name = "United States"), name = "Naruto", image = "hello")
        animeRepository.save(anime)

        expect(4) { animeRepository.getAll().size }
        checkNotNull { anime.uuid }
        expect("Naruto") { anime.name }
    }

    @Test
    fun saveAll() {
        assertThrows<Exception> {
            animeRepository.saveAll(
                listOf(
                    Anime(
                        country = Country(tag = "fr", name = "France"),
                        name = "Naruto",
                        image = "hello"
                    )
                )
            )
        }

        val anime1 = Anime(country = Country(tag = "us", name = "United States"), name = "Naruto", image = "hello")
        val anime2 = Anime(country = Country(tag = "uk", name = "United Kingdom"), name = "Bleach", image = "hello")
        animeRepository.saveAll(listOf(anime1, anime2))

        expect(5) { animeRepository.getAll().size }
        checkNotNull { anime1.uuid }
        checkNotNull { anime2.uuid }
        expect("Naruto") { anime1.name }
        expect("Bleach") { anime2.name }
    }

    @Test
    fun delete() {
        assertThrows<Exception> {
            animeRepository.delete(
                Anime(
                    country = Country(tag = "fr", name = "France"),
                    name = "Naruto",
                    image = "hello"
                )
            )
        }

        val animes = animeRepository.getAll()
        animeRepository.delete(animes.first())
        expect(2) { animes.size - 1 }
    }

    @Test
    fun deleteAll() {
        assertThrows<Exception> {
            animeRepository.deleteAll(
                listOf(
                    Anime(
                        country = Country(tag = "fr", name = "France"),
                        name = "Naruto",
                        image = "hello"
                    )
                )
            )
        }

        val animes = animeRepository.getAll()
        animeRepository.deleteAll(listOf(animes.first(), animes[1]))
        expect(1) { animes.size - 2 }
    }

    @Test
    fun findByHash() {
        val anime2 = animeRepository.findByHash("fr", "hello")
        checkNotNull { anime2 }
    }

    @Test
    fun findByName() {
        val animes = animeRepository.findByName("fr", "Naruto")
        expect(1) { animes.size }
    }

    @Test
    fun getByPage() {
        val simulcast = simulcastRepository.getAll().first()

        val page1 = animeRepository.getByPage("fr", simulcast.uuid, 1, 2)
        expect(2) { page1.size }
        val page2 = animeRepository.getByPage("fr", simulcast.uuid, 2, 2)
        expect(1) { page2.size }
    }

    @Test
    fun findByPage() {
        val animes = animeRepository.getAll()
        val uuids = listOf(animes[0].uuid, animes[1].uuid, animes[2].uuid)

        val page1 = animeRepository.findAllByPage(uuids, 1, 2)
        expect(2) { page1.size }
        val page2 = animeRepository.findAllByPage(uuids, 2, 2)
        expect(1) { page2.size }
    }

//    @Test
//    fun getDiary() {
//        val country = countryRepository.getAll().first()
//        val diary = animeRepository.getDiary(country.tag!!, Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
//        expect(3) { diary.size }
//    }
}
