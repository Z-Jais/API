package fr.ziedelth.plugins

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import fr.ziedelth.controllers.AbstractController
import fr.ziedelth.controllers.AttachmentController
import fr.ziedelth.dtos.AyaneDto
import fr.ziedelth.entities.*
import fr.ziedelth.entities.Platform
import fr.ziedelth.repositories.AbstractRepository
import fr.ziedelth.services.AbstractService
import fr.ziedelth.utils.Constant
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.Logger
import fr.ziedelth.utils.routes.*
import fr.ziedelth.utils.routes.method.Delete
import fr.ziedelth.utils.routes.method.Get
import fr.ziedelth.utils.routes.method.Post
import fr.ziedelth.utils.routes.method.Put
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.reflections.Reflections
import java.util.*
import java.util.logging.Level
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

private const val AN_ERROR_WAS_OCCURRED = "An error was occurred"

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
        createRoutes(reflections, injector)
    }
}

private fun Routing.createRoutes(reflections: Reflections, injector: Injector) {
    reflections.getSubTypesOf(AbstractController::class.java).forEach { controllerClass ->
        if (controllerClass.isAnnotationPresent(IgnorePath::class.java)) {
            return@forEach
        }

        val controller = injector.getInstance(controllerClass)
        createControllerRoutes(controller)
    }
}

private suspend fun isAuthorized(method: KFunction<*>, call: ApplicationCall): Boolean {
    if (method.hasAnnotation<Authorized>() && !Constant.secureKey.isNullOrBlank()) {
        val authorization = call.request.headers[HttpHeaders.Authorization]

        if (Constant.secureKey != authorization) {
            Logger.warning("Unauthorized request")
            call.respond(HttpStatusCode.Unauthorized, "Secure key not equals")
            return false
        }
    }

    return true
}

private suspend fun handleRequest(
    httpMethod: String,
    call: ApplicationCall,
    method: KFunction<*>,
    controller: AbstractController<*>,
    path: String
) {
    if (!isAuthorized(method, call)) return

    val parameters = call.parameters.toMap()
    val replacedPath = replacePathWithParameters("${controller.prefix}$path", parameters)

    Logger.info("$httpMethod $replacedPath")

    try {
        val response = callMethodWithParameters(method, controller, call, parameters)

        if (response is ResponseMultipart) {
            call.respondBytes(response.image, response.contentType)
        } else {
            call.respond(response.status, response.data ?: "")
        }
    } catch (e: Exception) {
        Logger.log(Level.SEVERE, AN_ERROR_WAS_OCCURRED, e)
        call.respond(HttpStatusCode.BadRequest)
    }
}

fun Routing.createControllerRoutes(controller: AbstractController<*>) {
    val isAttachmentController = controller::class.isSubclassOf(AttachmentController::class)
    val kMethods = controller::class.declaredFunctions.filter { it.hasAnnotation<Path>() }.toMutableSet()

    if (isAttachmentController) {
        kMethods.addAll(controller::class.superclasses.first().declaredFunctions.filter { it.hasAnnotation<Path>() })
    }

    route(controller.prefix) {
        kMethods.forEach { method ->
            val path = method.findAnnotation<Path>()!!.value

            if (method.hasAnnotation<Cached>()) {
                val cached = method.findAnnotation<Cached>()!!.maxAgeSeconds

                install(CachingHeaders) {
                    options { _, _ -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = cached)) }
                }
            }

            if (method.hasAnnotation<Get>()) {
                get(path) {
                    handleRequest("GET", call, method, controller, path)
                }
            }

            if (method.hasAnnotation<Post>()) {
                post(path) {
                    handleRequest("POST", call, method, controller, path)
                }
            }

            if (method.hasAnnotation<Put>()) {
                put(path) {
                    handleRequest("PUT", call, method, controller, path)
                }
            }

            if (method.hasAnnotation<Delete>()) {
                delete(path) {
                    handleRequest("DELETE", call, method, controller, path)
                }
            }
        }
    }
}

private fun replacePathWithParameters(path: String, parameters: Map<String, List<String>>): String =
    parameters.keys.fold(path) { acc, param ->
        acc.replace("{$param}", parameters[param]!!.joinToString(", "))
    }

private suspend fun callMethodWithParameters(
    method: KFunction<*>,
    controller: AbstractController<*>,
    call: ApplicationCall,
    parameters: Map<String, List<String>>
): Response {
    val methodParams = method.parameters.associateWith { kParameter ->
        if (kParameter.name.isNullOrBlank()) {
            controller
        } else if (kParameter.name == "body") {
            when (kParameter.type.javaType) {
                Anime::class.java -> call.receive<Anime>()
                Array<UUID>::class.java -> call.receive<Array<UUID>>()
                AyaneDto::class.java -> call.receive<AyaneDto>()
                Country::class.java -> call.receive<Country>()
                Array<Episode>::class.java -> call.receive<Array<Episode>>()
                EpisodeType::class.java -> call.receive<EpisodeType>()
                Genre::class.java -> call.receive<Genre>()
                LangType::class.java -> call.receive<LangType>()
                Platform::class.java -> call.receive<Platform>()
                else -> call.receive<String>()
            }
        } else {
            val value = parameters[kParameter.name]!!.first()

            val parsedValue = when (kParameter.type.javaType) {
                UUID::class.java -> UUID.fromString(value)
                Int::class.java -> value.toInt()
                else -> value
            }

            when (kParameter.name) {
                "page" -> require(parsedValue as Int >= 1) { "Page is not valid" }
                "limit" -> {
                    val i = parsedValue as Int
                    require(i in 1..30) { "Limit is not valid" }
                }
            }

            parsedValue
        }
    }

    method.isAccessible = true
    return method.callBy(methodParams) as Response
}
