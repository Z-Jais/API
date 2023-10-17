package fr.ziedelth

import com.google.inject.Guice
import com.microsoft.playwright.ElementHandle
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import fr.ziedelth.entities.Anime
import fr.ziedelth.plugins.DatabaseModule
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.repositories.ProfileRepository
import fr.ziedelth.services.RecommendationService
import fr.ziedelth.utils.Database
import org.reflections.Reflections
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    val database = Database()
    val reflections = Reflections("fr.ziedelth")
    val injector = Guice.createInjector(DatabaseModule(reflections, database))
    val animeRepository = injector.getInstance(AnimeRepository::class.java)
    val profileRepository = injector.getInstance(ProfileRepository::class.java)
    val recommendationService = injector.getInstance(RecommendationService::class.java)
    val csvFile = File("data/anime-genres-themes.csv")
    val text = if (csvFile.exists()) csvFile.readText() else ""

    val detectedAnimes = animeRepository.getAll()
        .filter { !text.contains(it.uuid.toString()) }
        .sortedByDescending { LocalDateTime.parse(it.releaseDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
        .take(50)

    if (detectedAnimes.isNotEmpty()) {
        val playwright = Playwright.create()
        val browser = playwright.firefox().launch()
        val page = browser.newPage()
        page.setDefaultTimeout(30_000.0)
        page.setDefaultNavigationTimeout(30_000.0)

        detectedAnimes.forEachIndexed { index, anime ->
            println("${anime.uuid} - ${anime.name} (${index + 1}/${detectedAnimes.size})")
            println("Please enter the MAL URL: ")
            val url = readlnOrNull()?.substringBeforeLast("?")?.replace("\n", "")

            if (url.isNullOrBlank()) {
                println("Skipping")
                return@forEachIndexed
            }

            val id = url.replace("https://", "").split("/")[2].toIntOrNull()

            if (id != null) {
                try {
                    navigateToPage(page, url)
                } catch (e: Exception) {
                    println("Skipping")
                    return@forEachIndexed
                }

                val spaceitPadDivs = page.querySelectorAll("div.spaceit_pad")
                val genres = genreThemeExtractor(spaceitPadDivs, "Genre")
                val themes = genreThemeExtractor(spaceitPadDivs, "Theme")

                println(genres)
                println(themes)

                buildCSV(csvFile, anime, id, url, genres, themes)
            }
        }

        page.close()
        browser.close()
        playwright.close()
    }


//    val profileAnimes = profileRepository.find(UUID.fromString("1ebafbf7-7367-4bf9-a1be-424c2c596e78"))!!.animes.mapNotNull { it.anime }
    val profileAnimes = listOf(
        animeRepository.findOneByName("fr", "The Eminence in Shadow")!!,
    )
    val recommendedAnimes = recommendationService.getRecommendations(profileAnimes)

    recommendedAnimes.forEach { recommendedAnime ->
        println("${recommendedAnime.anime.name} - ${recommendedAnime.score}")
    }
}

private fun buildCSV(file: File, anime: Anime, id: Int, url: String, genres: Set<String>, themes: Set<String>) {
    if (!file.exists()) {
        file.createNewFile()
        file.writeText("anime,mal-id,mal-url,genres,themes")
    }

    file.writeText(
        file.readText() + "\n\"${anime.uuid}\",\"$id\",\"$url\",\"${
            genres.joinToString(
                ","
            )
        }\",\"${themes.joinToString(",")}\""
    )
}

fun navigateToPage(page: Page, url: String) {
    println("Navigate to: $url")

    var `try` = 0
    var hasError: Boolean

    do {
        hasError = false

        try {
            `try`++
            page.navigate(url)
        } catch (e: Exception) {
            println("Error on navigate to $url: ${e.message}")
            hasError = true
        }
    } while (`try` <= 3 && hasError)

    if (hasError) throw Exception()

    page.waitForLoadState()
    botDetection(page)
}

fun botDetection(page: Page) {
    val isDetected = page.querySelector("#recaptcha-wrapper") != null

    if (isDetected) {
        println("WARNING! Bot detected!")
        readlnOrNull()
        page.reload()
        page.waitForLoadState()
    }
}

fun genreThemeExtractor(titleItems: List<ElementHandle>, prefix: String): MutableSet<String> {
    val list = titleItems.find { it.innerText().startsWith(prefix) }?.innerText()?.split(":")?.toMutableList()
        ?: mutableListOf()
    return if (list.size > 1) list[1].split(", ").map { it.trim() }.toMutableSet() else mutableSetOf()
}
