package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.DatabaseTest
import io.ktor.server.application.*
import io.ktor.server.routing.*

val session = DatabaseTest.getSession()
val countryRepository = CountryRepository(session)
val platformRepository = PlatformRepository(session)
val simulcastRepository = SimulcastRepository(session)
val genreRepository = GenreRepository(session)
val animeRepository = AnimeRepository(session)
val episodeTypeRepository = EpisodeTypeRepository(session)
val langTypeRepository = LangTypeRepository(session)
val episodeRepository = EpisodeRepository(session)
val mangaRepository = MangaRepository(session)
val newsRepository = NewsRepository(session)

fun Application.configureRoutingTest() {
    routing {
        CountryController(countryRepository).getRoutes(this)
        PlatformController(platformRepository).getRoutes(this)
        SimulcastController(simulcastRepository).getRoutes(this)
        GenreController(genreRepository).getRoutes(this)
        AnimeController(countryRepository, animeRepository, episodeRepository, mangaRepository).getRoutes(this)
        EpisodeTypeController(episodeTypeRepository).getRoutes(this)
        LangTypeController(langTypeRepository).getRoutes(this)
        EpisodeController(
            platformRepository,
            animeRepository,
            simulcastRepository,
            episodeTypeRepository,
            langTypeRepository,
            episodeRepository
        ).getRoutes(this)
        NewsController(countryRepository, platformRepository, newsRepository).getRoutes(this)
        MangaController(platformRepository, animeRepository, mangaRepository).getRoutes(this)
    }
}