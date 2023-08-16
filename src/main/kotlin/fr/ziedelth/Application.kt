package fr.ziedelth

import fr.ziedelth.listeners.ListenerManager
import fr.ziedelth.plugins.configureHTTP
import fr.ziedelth.plugins.configureRouting
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.RequestCache
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import nu.pattern.OpenCV
import java.util.*

private lateinit var database: Database

fun main(args: Array<String>) {
    val isDebug = args.isNotEmpty() && args[0] == "debug"
    val loadImage = !isDebug || (args.size > 1 && args[1] == "loadImage")

    if (isDebug) {
        println("DEBUG MODE")
    }

    println("Loading OpenCV...")
    OpenCV.loadShared()
    println("OpenCV loaded")
    println("Connecting to database...")
    database = Database()
    println("Database connected")
    if (loadImage) ImageCache.invalidCache(database)

    try {
        PluginManager.loadPlugins()
        ListenerManager()

        Thread {
            val scanner = Scanner(System.`in`)

            while (true) {
                val line = scanner.nextLine()

                if (line == "reload") {
                    PluginManager.reload()
                    ListenerManager()
                } else if (line == "invalid-cache") {
                    ImageCache.invalidCache(database)
                    RequestCache.clear()
                }
            }
        }.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    println("Starting server...")
    embeddedServer(
        Netty,
        port = if (isDebug) 37100 else 8080,
        host = "0.0.0.0",
        module = Application::myApplicationModule
    ).start(wait = true)
}

fun Application.myApplicationModule() {
    println("Configure server...")
    configureHTTP()
    println("Configure routing...")
    configureRouting(database)
    println("Server configured and ready")
}
