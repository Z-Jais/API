package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.plugins.animeRepository
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

    @Test
    fun getByPageWithAnime() {
        val anime = animeRepository.getAll().first()

        val page1 = episodeRepository.getByPageWithAnime(anime.uuid, 1, 2)
        expect(2) { page1.size }
        val page2 = episodeRepository.getByPageWithAnime(anime.uuid, 2, 2)
        expect(2) { page2.size }
    }

    @Test
    fun getByPageWithList() {
        val animes = animeRepository.getAll().take(2).map { it.uuid }

        val page1 = episodeRepository.getByPageWithList(animes, 1, 2)
        expect(2) { page1.size }
        val page2 = episodeRepository.getByPageWithList(animes, 2, 2)
        expect(2) { page2.size }
    }
}
