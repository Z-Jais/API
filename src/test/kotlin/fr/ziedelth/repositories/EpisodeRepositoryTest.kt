package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.plugins.episodeRepository
import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class EpisodeRepositoryTest : AbstractAPITest() {
    @Test
    fun getByPage() {
        val page1 = episodeRepository.getByPage("fr", 1, 2)
        expect(2) { page1.size }
        val page2 = episodeRepository.getByPage("fr", 2, 2)
        expect(2) { page2.size }
    }
}
