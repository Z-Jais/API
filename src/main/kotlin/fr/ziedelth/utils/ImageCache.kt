package fr.ziedelth.utils

import jakarta.persistence.Tuple
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.util.*

object ImageCache {
    data class Image(val url: String, val bytes: ByteArray = byteArrayOf(), val type: String = "jpg") {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Image) return false

            if (url != other.url) return false
            if (!bytes.contentEquals(other.bytes)) return false
            if (type != other.type) return false

            return true
        }

        override fun hashCode(): Int {
            var result = url.hashCode()
            result = 31 * result + bytes.contentHashCode()
            result = 31 * result + type.hashCode()
            return result
        }
    }

    private val cache = mutableMapOf<UUID, Pair<Image, Boolean>>()

    fun contains(uuid: UUID) = cache.containsKey(uuid)

    fun get(uuid: UUID): Image? {
        val pair = cache[uuid] ?: return null

        if (pair.second) {
            return pair.first
        }

        Logger.info("Encoding image to WebP")
        val url = pair.first.url
        var bytes: ByteArray? = null

        try {
            bytes = saveImage(url).readBytes()
            if (bytes.isEmpty()) throw Exception("Failed to load image")

            val webp = encodeToWebP(bytes)
            cache[uuid] = Image(url, webp, "webp") to true
        } catch (e: Exception) {
            if (bytes != null) {
                cache[uuid] = Image(url, bytes, "jpg") to true
            } else {
                Logger.warning("Failed to load image $url : ${e.message}")
            }
        }

        return cache[uuid]?.first
    }

    private fun encodeToWebP(image: ByteArray): ByteArray {
        val matImage = Imgcodecs.imdecode(MatOfByte(*image), Imgcodecs.IMREAD_UNCHANGED)
        val parameters = MatOfInt(Imgcodecs.IMWRITE_WEBP_QUALITY, 75)
        val output = MatOfByte()

        if (Imgcodecs.imencode(".webp", matImage, output, parameters)) {
            return output.toArray()
        } else {
            throw Exception("Failed to encode image to WebP")
        }
    }

    private fun saveImage(string: String): InputStream {
        val inputStream = URI(string).toURL().openStream()
        val tmpFile = Files.createTempFile(UUID.randomUUID().toString(), ".jpg").toFile()
        val outputStream = tmpFile.outputStream()
        outputStream.use { inputStream.copyTo(it) }
        return tmpFile.inputStream()
    }

    fun cache(uuid: UUID, url: String) {
        cache[uuid] = Image(url) to false
    }

    fun invalidCache(database: Database) {
        cache.clear()

        try {
            database.inTransaction { session ->
                // Get all platforms from database
                val platforms =
                    session.createQuery("SELECT uuid, image FROM Platform WHERE image LIKE 'http%'", Tuple::class.java)
                        .list()
                Logger.config("Platforms : ${platforms.size}")
                // Get all animes from database
                val animes = session.createQuery("SELECT uuid, image FROM Anime", Tuple::class.java).list()
                Logger.config("Animes : ${animes.size}")

                // Get all episodes from database
                val episodes = session.createQuery("SELECT uuid, image FROM Episode", Tuple::class.java).list()
                Logger.config("Episodes : ${episodes.size}")

                val combinedImages = platforms + animes + episodes

                combinedImages.forEach {
                    val uuid = it[0] as UUID
                    val url = it[1] as String
                    cache(uuid, url)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}