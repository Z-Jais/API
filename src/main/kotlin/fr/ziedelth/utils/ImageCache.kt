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

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }

    private val cache = mutableMapOf<UUID, Image>()
    private val newFixedThreadPool =
        Executors.newFixedThreadPool(max(1, Runtime.getRuntime().availableProcessors() - 1))
    var totalSize = 0

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

        newFixedThreadPool.submit {
            var bytes: ByteArray? = null

            try {
                bytes = saveImage(url).readBytes()
                val webp = encodeToWebP(bytes)
                cache[uuid] = Image(webp)
            } catch (e: Exception) {
                if (bytes != null) {
                    cache[uuid] = Image(bytes)
                } else {
                    println("Failed to load image $url : ${e.message}")
                    this.totalSize--
                }
            }
        }
    }

    fun startPrintProgressThread() {
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
}