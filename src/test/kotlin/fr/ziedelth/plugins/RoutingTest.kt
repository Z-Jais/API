package fr.ziedelth.plugins

import com.google.inject.Guice
import fr.ziedelth.controllers.AbstractController
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.DatabaseTest
import fr.ziedelth.utils.routes.IgnorePath
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.reflections.Reflections

val databaseTest = DatabaseTest()
private val reflections = Reflections("fr.ziedelth")
private val injector = Guice.createInjector(DatabaseModule(reflections, databaseTest))

val countryRepository: CountryRepository = injector.getInstance(CountryRepository::class.java)
val platformRepository: PlatformRepository = injector.getInstance(PlatformRepository::class.java)
val simulcastRepository: SimulcastRepository = injector.getInstance(SimulcastRepository::class.java)
val genreRepository: GenreRepository = injector.getInstance(GenreRepository::class.java)
val animeRepository: AnimeRepository = injector.getInstance(AnimeRepository::class.java)
val episodeTypeRepository: EpisodeTypeRepository = injector.getInstance(EpisodeTypeRepository::class.java)
val langTypeRepository: LangTypeRepository = injector.getInstance(LangTypeRepository::class.java)
val episodeRepository: EpisodeRepository = injector.getInstance(EpisodeRepository::class.java)
val profileRepository: ProfileRepository = injector.getInstance(ProfileRepository::class.java)

fun Application.configureRoutingTest() {
    routing {
        reflections.getSubTypesOf(AbstractController::class.java).forEach { controllerClass ->
            if (controllerClass.isAnnotationPresent(IgnorePath::class.java)) {
                return@forEach
            }

            val controller = injector.getInstance(controllerClass)
            createControllerRoutes(controller)
        }
    }
}