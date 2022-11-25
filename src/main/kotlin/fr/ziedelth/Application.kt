package fr.ziedelth

import fr.ziedelth.listeners.ListenerManager
import fr.ziedelth.plugins.configureHTTP
import fr.ziedelth.plugins.configureRouting
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import jakarta.persistence.Tuple
import nu.pattern.OpenCV
import java.util.*

fun main() {
    println("Loading OpenCV...")
    OpenCV.loadShared()
    println("OpenCV loaded")
    println("Connecting to database...")
    Database.getSessionFactory()
    println("Database connected")

    val session = Database.getSession()

    try {
        // Get all platforms from database
        val platforms =
            session.createQuery("SELECT uuid, image FROM Platform WHERE image LIKE 'http%'", Tuple::class.java).list()
        platforms.forEach { ImageCache.cachingNetworkImage(it[0] as UUID, it[1] as String) }
        println("Platforms : ${platforms.size}")

        // Get all animes from database
        val animes = session.createQuery("SELECT uuid, image FROM Anime", Tuple::class.java).list()
        animes.forEach { ImageCache.cachingNetworkImage(it[0] as UUID, it[1] as String) }
        println("Animes : ${animes.size}")

        // Get all episodes from database
        val episodes = session.createQuery("SELECT uuid, image FROM Episode", Tuple::class.java).list()
        episodes.forEach { ImageCache.cachingNetworkImage(it[0] as UUID, it[1] as String) }
        println("Episodes : ${episodes.size}")

        // Get all mangas from database
        val mangas = session.createQuery("SELECT uuid, cover FROM Manga", Tuple::class.java).list()
        mangas.forEach { ImageCache.cachingNetworkImage(it[0] as UUID, it[1] as String) }
        println("Mangas : ${mangas.size}")
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        session.close()
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
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        println("Configure server...")
        configureHTTP()
        println("Configure routing...")
        configureRouting()
        println("Server configured and ready")
    }.start(wait = true)
}
