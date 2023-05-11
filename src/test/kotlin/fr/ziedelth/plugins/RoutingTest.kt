package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.DatabaseTest
import io.ktor.server.application.*
import io.ktor.server.routing.*

val databaseTest = DatabaseTest()

val countryRepository = CountryRepository(databaseTest)
val platformRepository = PlatformRepository(databaseTest)
val simulcastRepository = SimulcastRepository(databaseTest)
val genreRepository = GenreRepository(databaseTest)
val animeRepository = AnimeRepository(databaseTest)
val episodeTypeRepository = EpisodeTypeRepository(databaseTest)
val langTypeRepository = LangTypeRepository(databaseTest)
val episodeRepository = EpisodeRepository(databaseTest)

fun Application.configureRoutingTest() {
    routing {
        CountryController(countryRepository).getRoutes(this)
        PlatformController(platformRepository).getRoutes(this)
        SimulcastController(simulcastRepository).getRoutes(this)
        GenreController(genreRepository).getRoutes(this)
        AnimeController(countryRepository, animeRepository, episodeRepository).getRoutes(this)
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
    }
}