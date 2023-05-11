package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.Notifications
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Application.configureRouting(database: Database) {
    routing {
        val countryRepository = CountryRepository(database)
        val platformRepository = PlatformRepository(database)
        val simulcastRepository = SimulcastRepository(database)
        val genreRepository = GenreRepository(database)
        val animeRepository = AnimeRepository(database)
        val episodeTypeRepository = EpisodeTypeRepository(database)
        val langTypeRepository = LangTypeRepository(database)
        val episodeRepository = EpisodeRepository(database)

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

        webSocket {
            val connection = Notifications.addConnection(this)
            println("New connection: ${connection.id} (${Notifications.connections.size})")

            try {
                while (true) {
                    val message = incoming.receive()
                    println(message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                Notifications.removeConnection(connection)
                println("Connection closed: ${connection.id} (${Notifications.connections.size})")
            }
        }
    }
}
