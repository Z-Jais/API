package fr.ziedelth

import fr.ziedelth.listeners.ListenerManager
import fr.ziedelth.plugins.configureHTTP
import fr.ziedelth.plugins.configureRouting
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.Logger
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
        Logger.warning("DEBUG MODE")
    }

    Logger.info("Loading OpenCV...")
    OpenCV.loadLocally()
    Logger.info("OpenCV loaded")
    Logger.info("Connecting to database...")
    database = Database()
    Logger.info("Database connected")
    if (loadImage) ImageCache.invalidCache(database)

    try {
        PluginManager.loadPlugins()
        ListenerManager()
        handleCommands()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    Logger.info("Starting server...")
    embeddedServer(
        Netty,
        port = if (isDebug) 37100 else 8080,
        host = "0.0.0.0",
        module = Application::myApplicationModule
    ).start(wait = true)
}

private fun handleCommands() {
    Thread {
        val scanner = Scanner(System.`in`)

        while (true) {
            try {
                val line = scanner.nextLine()

                if (line == "reload") {
                    PluginManager.reload()
                    ListenerManager()
                } else if (line == "invalid-cache") {
                    ImageCache.invalidCache(database)
                }
            } catch (_: Exception) {
                Thread.sleep(1000)
            }
        }
    }.start()
}

fun Application.myApplicationModule() {
    Logger.info("Configure server...")
    configureHTTP()
    Logger.info("Configure routing...")
    configureRouting(database)
    Logger.info("Server configured and ready")
}
