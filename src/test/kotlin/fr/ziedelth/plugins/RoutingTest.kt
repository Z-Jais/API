package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.controllers.EpisodeTypeController.getEpisodeTypes
import fr.ziedelth.controllers.GenreController.getGenres
import fr.ziedelth.controllers.LangTypeController.getLangTypes
import fr.ziedelth.controllers.SimulcastController.getSimulcasts
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.DatabaseTest
import io.ktor.server.application.*
import io.ktor.server.routing.*

val countryRepository = CountryRepository { DatabaseTest.getSession() }
val platformRepository = PlatformRepository { DatabaseTest.getSession() }
val simulcastRepository = SimulcastRepository { DatabaseTest.getSession() }
val animeRepository = AnimeRepository { DatabaseTest.getSession() }
val episodeTypeRepository = EpisodeTypeRepository { DatabaseTest.getSession() }
val langTypeRepository = LangTypeRepository { DatabaseTest.getSession() }
val episodeRepository = EpisodeRepository { DatabaseTest.getSession() }
val mangaRepository = MangaRepository { DatabaseTest.getSession() }

fun Application.configureRoutingTest() {
    routing {
        CountryController(countryRepository).getRoutes(this)
        PlatformController(platformRepository).getRoutes(this)
        getSimulcasts()
        getGenres()
        AnimeController(countryRepository, animeRepository, episodeRepository, mangaRepository).getRoutes(this)
        getEpisodeTypes()
        getLangTypes()
        EpisodeController(animeRepository).getRoutes(this)
        NewsController(countryRepository).getRoutes(this)
        MangaController(animeRepository).getRoutes(this)
    }
}