package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.plugins.countryRepository
import fr.ziedelth.plugins.simulcastRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.expect

internal class SimulcastRepositoryTest : AbstractAPITest() {
    @Test
    fun find() {
        val simulcast = simulcastRepository.find(UUID.randomUUID())
        expect(null) { simulcast }

        val simulcasts = simulcastRepository.getAll()
        expect(simulcasts.first().uuid) { simulcastRepository.find(simulcasts.first().uuid)?.uuid }
    }

    @Test
    fun exists() {
        expect(true) { simulcastRepository.exists("year", 2020) }
        expect(false) { simulcastRepository.exists("uuid", UUID.randomUUID()) }
    }

    @Test
    fun findAll() {
        expect(emptyList()) { simulcastRepository.findAll(listOf(UUID.randomUUID())) }

        val simulcasts = simulcastRepository.getAll()
        val list = simulcastRepository.findAll(listOf(simulcasts[0].uuid, simulcasts[1].uuid))

        expect(true) { list.any { it.uuid == simulcasts.first().uuid } && list.any { it.uuid == simulcasts[1].uuid } }
    }

    @Test
    fun getAll() {
        expect(2) { simulcastRepository.getAll().size }
    }

    @Test
    fun save() {
        assertThrows<Exception> {
            simulcastRepository.save(Simulcast(year = 2020, season = "WINTER"))
        }

        val simulcast = Simulcast.getSimulcast(2021, 1)
        simulcastRepository.save(simulcast)

        expect(3) { simulcastRepository.getAll().size }
        checkNotNull { simulcast.uuid }
        expect("WINTER") { simulcast.season }
    }

    @Test
    fun saveAll() {
        assertThrows<Exception> {
            simulcastRepository.saveAll(
                listOf(
                    Simulcast(year = 2020, season = "WINTER"),
                    Simulcast(year = 2020, season = "SPRING")
                )
            )
        }

        val simulcast1 = Simulcast.getSimulcast(2019, 1)
        val simulcast2 = Simulcast.getSimulcast(2019, 11)
        simulcastRepository.saveAll(listOf(simulcast1, simulcast2))

        expect(4) { simulcastRepository.getAll().size }
        checkNotNull { simulcast1.uuid }
        checkNotNull { simulcast2.uuid }
        expect("WINTER") { simulcast1.season }
        expect("AUTUMN") { simulcast2.season }
    }

    @Test
    fun delete() {
        assertThrows<Exception> {
            simulcastRepository.delete(
                Simulcast(
                    UUID.randomUUID(),
                    season = "WINTER",
                    year = 2020
                )
            )
        }

        val simulcasts = simulcastRepository.getAll()
        simulcastRepository.delete(simulcasts.first())
        expect(1) { simulcasts.size - 1 }
    }

    @Test
    fun getAllByTag() {
        val country = countryRepository.getAll().first()
        val simulcasts = simulcastRepository.getAll(country.tag)
        expect(1) { simulcasts.size }
        expect("WINTER") { simulcasts.first().season }
    }

    @Test
    fun findBySeasonAndYear() {
        val simulcast = simulcastRepository.findBySeasonAndYear("WINTER", 2020)
        checkNotNull(simulcast?.uuid)
    }
}
