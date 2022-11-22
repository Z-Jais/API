package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Country
import fr.ziedelth.plugins.countryRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.expect

internal class CountryRepositoryTest : AbstractAPITest() {
    @Test
    fun find() {
        val country = countryRepository.find(UUID.randomUUID())
        expect(null) { country }

        val countries = countryRepository.getAll()
        expect(countries.first().uuid) { countryRepository.find(countries.first().uuid)?.uuid }
    }

    @Test
    fun exists() {
        expect(true) { countryRepository.exists("tag", "fr") }
        expect(false) { countryRepository.exists("uuid", UUID.randomUUID()) }
    }

    @Test
    fun findAll() {
        expect(emptyList()) { countryRepository.findAll(listOf(UUID.randomUUID())) }

        val countries = countryRepository.getAll()
        val list = countryRepository.findAll(listOf(countries[0].uuid, countries[1].uuid))

        expect(true) { list.any { it.uuid == countries.first().uuid } && list.any { it.uuid == countries[1].uuid } }
    }

    @Test
    fun getAll() {
        expect(2) { countryRepository.getAll().size }
    }

    @Test
    fun save() {
        assertThrows<Exception> {
            countryRepository.save(Country(tag = "fr", name = "France"))
        }

        val country = Country(tag = "us", name = "United States")
        countryRepository.save(country)

        expect(3) { countryRepository.getAll().size }
        checkNotNull { country.uuid }
        expect("United States") { country.name }
    }

    @Test
    fun saveAll() {
        assertThrows<Exception> {
            countryRepository.saveAll(
                listOf(
                    Country(tag = "fr", name = "France"),
                    Country(tag = "jp", name = "Japan")
                )
            )
        }

        val country1 = Country(tag = "us", name = "United States")
        val country2 = Country(tag = "uk", name = "United Kingdom")
        countryRepository.saveAll(listOf(country1, country2))

        expect(4) { countryRepository.getAll().size }
        checkNotNull { country1.uuid }
        checkNotNull { country2.uuid }
        expect("United States") { country1.name }
        expect("United Kingdom") { country2.name }
    }

    @Test
    fun delete() {
        assertThrows<Exception> {
            countryRepository.delete(
                Country(
                    tag = "fr",
                    name = "France"
                )
            )
        }

        val countries = countryRepository.getAll()
        countryRepository.delete(countries.first())
        expect(1) { countries.size - 1 }
    }
}
