package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.plugins.animeRepository
import fr.ziedelth.plugins.mangaRepository
import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class MangaRepositoryTest : AbstractAPITest() {
    @Test
    fun getByPage() {
        val page1 = mangaRepository.getByPage("fr", 1, 2)
        expect(2) { page1.size }
        val page2 = mangaRepository.getByPage("fr", 2, 2)
        expect(2) { page2.size }
    }

    @Test
    fun getByPageWithAnime() {
        val anime = animeRepository.getAll().first()

        val page1 = mangaRepository.getByPageWithAnime(anime.uuid, 1, 2)
        expect(2) { page1.size }
        val page2 = mangaRepository.getByPageWithAnime(anime.uuid, 2, 2)
        expect(2) { page2.size }
    }

    @Test
    fun getByPageWithList() {
        val mangas = mangaRepository.getAll().take(3).map { it.uuid }

        val page1 = mangaRepository.getByPageWithList(mangas, 1, 2)
        expect(2) { page1.size }
        val page2 = mangaRepository.getByPageWithList(mangas, 2, 2)
        expect(1) { page2.size }
    }
}
