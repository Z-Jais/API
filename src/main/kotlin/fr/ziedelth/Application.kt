package fr.ziedelth

import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Manga
import fr.ziedelth.plugins.configureHTTP
import fr.ziedelth.plugins.configureRouting
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.ImageCache
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import nu.pattern.OpenCV

fun main() {
    println("Loading OpenCV...")
    OpenCV.loadShared()
    println("OpenCV loaded")
    println("Connecting to database...")
    Database.getSessionFactory()
    println("Database connected")

    val session = Database.getSession()

    try {
        // Get all animes from database
        val animes = session.createQuery("FROM Anime", Anime::class.java).list()
        animes.forEach { ImageCache.cachingNetworkImage(it.uuid, it.image!!) }
        println("Animes : ${animes.size}")

        // Get all episodes from database
        val episodes = session.createQuery("FROM Episode", Episode::class.java).list()
        episodes.forEach { ImageCache.cachingNetworkImage(it.uuid, it.image!!) }
        println("Episodes : ${episodes.size}")

        // Get all mangas from database
        val mangas = session.createQuery("FROM Manga", Manga::class.java).list()
        mangas.forEach { ImageCache.cachingNetworkImage(it.uuid, it.cover!!) }
        println("Mangas : ${mangas.size}")
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        session.close()
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
