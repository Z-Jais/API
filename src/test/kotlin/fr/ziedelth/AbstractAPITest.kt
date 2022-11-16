package fr.ziedelth

import fr.ziedelth.entities.*
import fr.ziedelth.plugins.*
import fr.ziedelth.utils.DatabaseTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal abstract class AbstractAPITest {
    @BeforeEach
    fun tearUp() {
        countryRepository.saveAll(
            listOf(
                Country(tag = "fr", name = "France"),
                Country(tag = "jp", name = "Japan")
            )
        )
        val countries = countryRepository.getAll()

        val platform1 = Platform(name = "Netflix", image = "hello", url = "hello")
        val platform2 = Platform(name = "Crunchyroll", image = "hello", url = "hello")
        val platform3 = Platform(name = "Wakanim", image = "hello", url = "hello")
        platformRepository.saveAll(listOf(platform1, platform2, platform3))
        val platforms = platformRepository.getAll()

        val simulcast1 = Simulcast(season = "WINTER", year = 2020)
        val simulcast2 = Simulcast(season = "SPRING", year = 2020)
        simulcastRepository.saveAll(listOf(simulcast1, simulcast2))
        val simulcasts = simulcastRepository.getAll()

        val genre1 = Genre(name = "Action")
        val genre2 = Genre(name = "Comedy")
        val genre3 = Genre(name = "Drama")
        genreRepository.saveAll(listOf(genre1, genre2, genre3))
        val genres = genreRepository.getAll()

        val anime1 = Anime(
            country = countries.first(),
            name = "One Piece",
            image = "hello",
            hashes = mutableSetOf("hello"),
            simulcasts = mutableSetOf(simulcasts.first()),
            genres = mutableSetOf(genres.first(), genres.last()),
        )
        val anime2 = Anime(
            country = countries.first(),
            name = "Naruto",
            image = "hello",
            hashes = mutableSetOf("hello2"),
            simulcasts = mutableSetOf(simulcasts.first()),
            genres = mutableSetOf(genres.first(), genres.last()),
        )
        val anime3 = Anime(
            country = countries.first(),
            name = "Bleach",
            image = "hello",
            hashes = mutableSetOf("hello3"),
            simulcasts = mutableSetOf(simulcasts.first()),
            genres = mutableSetOf(genres.first(), genres.last()),
        )
        animeRepository.saveAll(listOf(anime1, anime2, anime3))
        val animes = animeRepository.getAll()

        val episodeType1 = EpisodeType(name = "Episode")
        val episodeType2 = EpisodeType(name = "OAV")
        val episodeType3 = EpisodeType(name = "Film")
        episodeTypeRepository.saveAll(listOf(episodeType1, episodeType2, episodeType3))
        val episodeTypes = episodeTypeRepository.getAll()

        val langType1 = LangType(name = "SUBTITLES")
        val langType2 = LangType(name = "VOICE")
        langTypeRepository.saveAll(listOf(langType1, langType2))
        val langTypes = langTypeRepository.getAll()

        animes.forEach {
            val episodes = (1..10).map { episode ->
                val platform = platforms.random()
                val episodeType = episodeTypes.random()
                val langType = langTypes.random()

                Episode(
                    platform = platform,
                    anime = it,
                    episodeType = episodeType,
                    langType = langType,
                    hash = "EP-$episode-${platform.name}-${episodeType.name}-${langType.name}-${Math.random()}",
                    season = 1,
                    number = episode,
                    url = "hello",
                    image = "hello",
                    duration = 1440
                )
            }
            episodeRepository.saveAll(episodes)
        }
    }

    @AfterEach
    fun tearDown() {
        DatabaseTest.clean()
    }
}