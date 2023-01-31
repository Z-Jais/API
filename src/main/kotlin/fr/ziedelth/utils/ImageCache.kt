package fr.ziedelth.utils

import org.opencv.core.MatOfByte
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.max

object ImageCache {
    data class Image(val bytes: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Image

            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }

    private val cache = mutableMapOf<UUID, Image>()
    private val newFixedThreadPool =
        Executors.newFixedThreadPool(max(1, Runtime.getRuntime().availableProcessors() - 1))

    fun contains(uuid: UUID) = cache.containsKey(uuid)
    fun get(uuid: UUID) = cache[uuid]

    private fun encodeToWebP(image: ByteArray): ByteArray {
        val matImage = Imgcodecs.imdecode(MatOfByte(*image), Imgcodecs.IMREAD_UNCHANGED)
        val parameters = MatOfInt(Imgcodecs.IMWRITE_WEBP_QUALITY, 75)
        val output = MatOfByte()

        if (Imgcodecs.imencode(".webp", matImage, output, parameters)) {
            return output.toArray()
        } else {
            throw IllegalStateException("Failed to encode the image as webp")
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

        newFixedThreadPool.submit {
            try {
                val bytes = saveImage(url).readBytes()
                cache[uuid] = Image(bytes)

                val webp = encodeToWebP(bytes)
                cache[uuid] = Image(webp)
            } catch (e: Exception) {
                println("Failed to load image $url : ${e.message}")
            }
        }
    }
}