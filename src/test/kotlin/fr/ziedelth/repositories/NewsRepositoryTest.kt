package fr.ziedelth.repositories

import fr.ziedelth.AbstractAPITest
import fr.ziedelth.plugins.newsRepository
import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class NewsRepositoryTest : AbstractAPITest() {
    @Test
    fun getByPage() {
        val page1 = newsRepository.getByPage("fr", 1, 2)
        expect(2) { page1.size }
        val page2 = newsRepository.getByPage("fr", 2, 2)
        expect(0) { page2.size }
    }
}
