package fr.ziedelth

import fr.ziedelth.listeners.ListenerManager
import fr.ziedelth.plugins.configureHTTP
import fr.ziedelth.plugins.configureRouting
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import jakarta.persistence.Tuple
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

    try {
        database.inTransaction { session ->
            // Get all platforms from database
            val platforms =
                session.createQuery("SELECT uuid, image FROM Platform WHERE image LIKE 'http%'", Tuple::class.java)
                    .list()
            println("Platforms : ${platforms.size}")
            // Get all animes from database
            val animes = session.createQuery("SELECT uuid, image FROM Anime", Tuple::class.java).list()
            println("Animes : ${animes.size}")

            // Get all episodes from database
            val episodes = session.createQuery("SELECT uuid, image FROM Episode", Tuple::class.java).list()
            println("Episodes : ${episodes.size}")

            if (loadImage) {
                platforms.forEach { ImageCache.cachingNetworkImage(it[0] as UUID, it[1] as String) }
                animes.forEach { ImageCache.cachingNetworkImage(it[0] as UUID, it[1] as String) }
                episodes.forEach { ImageCache.cachingNetworkImage(it[0] as UUID, it[1] as String) }
                ImageCache.totalSize = platforms.size + animes.size + episodes.size
                ImageCache.startPrintProgressThread()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

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
