package fr.ziedelth.plugins

import com.google.inject.AbstractModule
import com.google.inject.Guice
import fr.ziedelth.controllers.AbstractController
import fr.ziedelth.controllers.AttachmentController
import fr.ziedelth.repositories.AbstractRepository
import fr.ziedelth.services.AbstractService
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.routes.APIIgnore
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.reflections.Reflections
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.jvm.javaMethod

class DatabaseModule(private val reflections: Reflections, private val database: Database) : AbstractModule() {
    override fun configure() {
        bind(Database::class.java).toInstance(database)

        reflections.getSubTypesOf(AbstractRepository::class.java).forEach {
            bind(it).asEagerSingleton()
        }

        reflections.getSubTypesOf(AbstractService::class.java).forEach {
            bind(it).asEagerSingleton()
        }
    }
}

fun Application.configureRouting(database: Database) {
    val reflections = Reflections("fr.ziedelth")
    val injector = Guice.createInjector(DatabaseModule(reflections, database))

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
