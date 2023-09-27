package fr.ziedelth.plugins

import com.google.inject.Guice
import fr.ziedelth.controllers.AbstractController
import fr.ziedelth.controllers.AttachmentController
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.DatabaseTest
import fr.ziedelth.utils.routes.APIIgnore
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.reflections.Reflections
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.jvm.javaMethod

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

fun Application.configureRoutingTest() {
    routing {
        reflections.getSubTypesOf(AbstractController::class.java).forEach {
            if (it.isAnnotationPresent(APIIgnore::class.java)) {
                return@forEach
            }

            val controller = injector.getInstance(it)
            val isAttachmentController = controller::class.java.superclass == AttachmentController::class.java

            val kFunctions = controller::class.memberExtensionFunctions.filter { kFunction ->
                kFunction.findAnnotations(APIRoute::class).isNotEmpty()
            }.toMutableList()

            route(controller.prefix) {
                kFunctions.forEach { kFunction ->
                    val javaMethod = kFunction.javaMethod!!
                    javaMethod.isAccessible = true
                    javaMethod.invoke(controller, this)
                }

                if (isAttachmentController) {
                    controller.javaClass.getMethod("attachmentByUUID", Route::class.java).invoke(controller, this)
                }
            }
        }
    }
}