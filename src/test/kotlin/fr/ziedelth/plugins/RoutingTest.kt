package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.DatabaseTest
import io.ktor.server.application.*
import io.ktor.server.routing.*

val countryRepository = CountryRepository { DatabaseTest.getSession() }
val platformRepository = PlatformRepository { DatabaseTest.getSession() }
val simulcastRepository = SimulcastRepository { DatabaseTest.getSession() }
val genreRepository = GenreRepository { DatabaseTest.getSession() }
val animeRepository = AnimeRepository { DatabaseTest.getSession() }
val episodeTypeRepository = EpisodeTypeRepository { DatabaseTest.getSession() }
val langTypeRepository = LangTypeRepository { DatabaseTest.getSession() }
val episodeRepository = EpisodeRepository { DatabaseTest.getSession() }
val mangaRepository = MangaRepository { DatabaseTest.getSession() }

fun Application.configureRoutingTest() {
    routing {
        CountryController(countryRepository).getRoutes(this)
        PlatformController(platformRepository).getRoutes(this)
        SimulcastController(simulcastRepository).getRoutes(this)
        GenreController(genreRepository).getRoutes(this)
        AnimeController(countryRepository, animeRepository, episodeRepository, mangaRepository).getRoutes(this)
        EpisodeTypeController(episodeTypeRepository).getRoutes(this)
        LangTypeController(langTypeRepository).getRoutes(this)
        EpisodeController(platformRepository, animeRepository, simulcastRepository, episodeTypeRepository, langTypeRepository, episodeRepository).getRoutes(this)
        NewsController(countryRepository, platformRepository).getRoutes(this)
        MangaController(platformRepository, animeRepository).getRoutes(this)
    }
}