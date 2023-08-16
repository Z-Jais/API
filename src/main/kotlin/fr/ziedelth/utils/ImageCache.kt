package fr.ziedelth.utils

import jakarta.persistence.Tuple
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.util.*

object ImageCache {
    data class Image(val bytes: ByteArray, val type: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Image

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }

    private val cache = mutableMapOf<UUID, Image>()
    private var totalSize = 0

    fun contains(uuid: UUID) = cache.containsKey(uuid)
    fun get(uuid: UUID) = cache[uuid]

    private fun encodeToWebP(image: ByteArray): ByteArray {
        val matImage = Imgcodecs.imdecode(MatOfByte(*image), Imgcodecs.IMREAD_UNCHANGED)
        val parameters = MatOfInt(Imgcodecs.IMWRITE_WEBP_QUALITY, 75)
        val output = MatOfByte()

        if (Imgcodecs.imencode(".webp", matImage, output, parameters)) {
            return output.toArray()
        } else {
            error("Failed to encode the image as webp")
        }
    }

    private fun saveImage(string: String?): InputStream {
        val inputStream = URL(string).openStream()
        val tmpFile = Files.createTempFile(UUID.randomUUID().toString(), ".jpg").toFile()
        val outputStream = tmpFile.outputStream()
        outputStream.use { inputStream.copyTo(it) }
        return tmpFile.inputStream()
    }

    fun cachingNetworkImage(uuid: UUID, url: String) {
        if (contains(uuid)) return
        var bytes: ByteArray? = null

        try {
            bytes = saveImage(url).readBytes()
            val webp = encodeToWebP(bytes)
            cache[uuid] = Image(webp, "webp")
        } catch (e: Exception) {
            if (bytes != null) {
                cache[uuid] = Image(bytes, "jpg")
            } else {
                println("Failed to load image $url : ${e.message}")
                this.totalSize--
            }
        }
    }

    private fun startPrintProgressThread() {
        val thread = Thread {
            val marginError = 5 // In percent
            var totalSize: Double
            var isRunning = true

            while (isRunning) {
                totalSize = this.totalSize * (1 - marginError / 100.0)
                isRunning = cache.size < totalSize
                println("Progress : ${cache.size}/${this.totalSize} (${totalSize.toInt()} with $marginError% margin error)")
                if (!isRunning) println("Done")
                Thread.sleep(5000)
            }
        }

        thread.isDaemon = true
        thread.start()
    }

    fun invalidCache(database: Database) {
        cache.clear()
        totalSize = 0

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

                val combinedImages = platforms + animes + episodes
                totalSize = combinedImages.size
                startPrintProgressThread()

                combinedImages.parallelStream().forEach {
                    cachingNetworkImage(it[0] as UUID, it[1] as String)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}