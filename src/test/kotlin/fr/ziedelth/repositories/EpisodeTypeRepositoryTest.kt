package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.plugins.episodeTypeRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.expect

internal class EpisodeTypeRepositoryTest : AbstractAPITest() {
    @Test
    fun find() {
        val episodeType = episodeTypeRepository.find(UUID.randomUUID())
        expect(null) { episodeType }

        val episodeTypes = episodeTypeRepository.getAll()
        expect(episodeTypes.first().uuid) { episodeTypeRepository.find(episodeTypes.first().uuid)?.uuid }
    }

    @Test
    fun exists() {
        expect(true) { episodeTypeRepository.exists("name", "Episode") }
        expect(false) { episodeTypeRepository.exists("uuid", UUID.randomUUID()) }
    }

    @Test
    fun findAll() {
        expect(emptyList()) { episodeTypeRepository.findAll(listOf(UUID.randomUUID())) }

        val episodeTypes = episodeTypeRepository.getAll()
        val list = episodeTypeRepository.findAll(listOf(episodeTypes[0].uuid, episodeTypes[1].uuid))

        expect(true) { list.any { it.uuid == episodeTypes.first().uuid } && list.any { it.uuid == episodeTypes[1].uuid } }
    }

    @Test
    fun getAll() {
        expect(3) { episodeTypeRepository.getAll().size }
    }

    @Test
    fun save() {
        assertThrows<Exception> {
            episodeTypeRepository.save(EpisodeType(name = "Episode"))
        }

        val episodeType = EpisodeType(name = "Special")
        episodeTypeRepository.save(episodeType)

        expect(4) { episodeTypeRepository.getAll().size }
        checkNotNull { episodeType.uuid }
        expect("Special") { episodeType.name }
    }

    @Test
    fun saveAll() {
        assertThrows<Exception> {
            episodeTypeRepository.saveAll(
                listOf(
                    EpisodeType(name = "Episode"),
                    EpisodeType(name = "OAV")
                )
            )
        }

        val episodeType1 = EpisodeType(name = "Special")
        val episodeType2 = EpisodeType(name = "Special 2")
        episodeTypeRepository.saveAll(listOf(episodeType1, episodeType2))

        expect(5) { episodeTypeRepository.getAll().size }
        checkNotNull { episodeType1.uuid }
        checkNotNull { episodeType2.uuid }
        expect("Special") { episodeType1.name }
        expect("Special 2") { episodeType2.name }
    }

    @Test
    fun delete() {
        assertThrows<Exception> {
            episodeTypeRepository.delete(
                EpisodeType(
                    name = "Episode"
                )
            )
        }

        val episodeTypes = episodeTypeRepository.getAll()
        episodeTypeRepository.delete(episodeTypes.first())
        expect(2) { episodeTypes.size - 1 }
    }
}
