package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.LangType
import fr.ziedelth.plugins.langTypeRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.expect

internal class LangTypeRepositoryTest : AbstractAPITest() {
    @Test
    fun find() {
        val langType = langTypeRepository.find(UUID.randomUUID())
        expect(null) { langType }

        val langTypes = langTypeRepository.getAll()
        expect(langTypes.first().uuid) { langTypeRepository.find(langTypes.first().uuid)?.uuid }
    }

    @Test
    fun exists() {
        expect(true) { langTypeRepository.exists("name", "SUBTITLES") }
        expect(false) { langTypeRepository.exists("uuid", UUID.randomUUID()) }
    }

    @Test
    fun findAll() {
        expect(emptyList()) { langTypeRepository.findAll(listOf(UUID.randomUUID())) }

        val langTypes = langTypeRepository.getAll()
        val list = langTypeRepository.findAll(listOf(langTypes[0].uuid, langTypes[1].uuid))

        expect(true) { list.any { it.uuid == langTypes.first().uuid } && list.any { it.uuid == langTypes[1].uuid } }
    }

    @Test
    fun getAll() {
        expect(2) { langTypeRepository.getAll().size }
    }

    @Test
    fun save() {
        assertThrows<Exception> {
            langTypeRepository.save(LangType(name = "SUBTITLES"))
        }

        val langType = LangType(name = "DUB")
        langTypeRepository.save(langType)

        expect(3) { langTypeRepository.getAll().size }
        checkNotNull { langType.uuid }
        expect("DUB") { langType.name }
    }

    @Test
    fun saveAll() {
        assertThrows<Exception> {
            langTypeRepository.saveAll(
                listOf(
                    LangType(name = "SUBTITLES"),
                    LangType(name = "VOICE")
                )
            )
        }

        val langType1 = LangType(name = "DUB")
        val langType2 = LangType(name = "DUB2")
        langTypeRepository.saveAll(listOf(langType1, langType2))

        expect(4) { langTypeRepository.getAll().size }
        checkNotNull { langType1.uuid }
        checkNotNull { langType2.uuid }
        expect("DUB") { langType1.name }
        expect("DUB2") { langType2.name }
    }

    @Test
    fun delete() {
        assertThrows<Exception> {
            langTypeRepository.delete(
                LangType(
                    name = "SUBTITLES"
                )
            )
        }

        val langTypes = langTypeRepository.getAll()
        langTypeRepository.delete(langTypes.first())
        expect(1) { langTypes.size - 1 }
    }
}
