package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.entities.Platform
import fr.ziedelth.plugins.platformRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.expect

internal class PlatformRepositoryTest : AbstractAPITest() {
    @Test
    fun find() {
        val platform = platformRepository.find(UUID.randomUUID())
        expect(null) { platform }

        val platforms = platformRepository.getAll()
        expect(platforms.first().uuid) { platformRepository.find(platforms.first().uuid)?.uuid }
    }

    @Test
    fun exists() {
        expect(true) { platformRepository.exists("name", "Netflix") }
        expect(false) { platformRepository.exists("uuid", UUID.randomUUID()) }
    }

    @Test
    fun findAll() {
        expect(emptyList()) { platformRepository.findAll(listOf(UUID.randomUUID())) }

        val platforms = platformRepository.getAll()
        val list = platformRepository.findAll(listOf(platforms[0].uuid, platforms[1].uuid))

        expect(true) { list.any { it.uuid == platforms.first().uuid } && list.any { it.uuid == platforms[1].uuid } }
    }

    @Test
    fun getAll() {
        expect(3) { platformRepository.getAll().size }
    }

    @Test
    fun save() {
        assertThrows<Exception> {
            platformRepository.save(
                Platform(
                    name = "Netflix",
                    image = "hello",
                    url = "hello"
                )
            )
        }

        val platform = Platform(name = "Animation Digital Network", image = "hello", url = "hello")
        platformRepository.save(platform)

        expect(4) { platformRepository.getAll().size }
        checkNotNull { platform.uuid }
        expect("Animation Digital Network") { platform.name }
    }

    @Test
    fun saveAll() {
        assertThrows<Exception> {
            platformRepository.saveAll(
                listOf(
                    Platform(
                        name = "Netflix",
                        image = "hello",
                        url = "hello"
                    ),
                    Platform(
                        name = "Crunchyroll",
                        image = "hello",
                        url = "hello"
                    )
                )
            )
        }

        val platform1 = Platform(name = "Animation Digital Network", image = "hello", url = "hello")
        val platform2 = Platform(name = "MangaNews", image = "hello", url = "hello")
        platformRepository.saveAll(listOf(platform1, platform2))

        expect(5) { platformRepository.getAll().size }
        checkNotNull { platform1.uuid }
        checkNotNull { platform2.uuid }
        expect("Animation Digital Network") { platform1.name }
        expect("MangaNews") { platform2.name }
    }

    @Test
    fun delete() {
        assertThrows<Exception> {
            platformRepository.delete(
                Platform(
                    name = "Netflix",
                    image = "hello",
                    url = "hello"
                )
            )
        }

        val platforms = platformRepository.getAll()
        platformRepository.delete(platforms.first())
        expect(2) { platforms.size - 1 }
    }
}
