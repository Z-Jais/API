package fr.ziedelth.utils

import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPOutputStream

object Encoder {
    private fun base64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    fun toGzip(string: String): String {
        val bos = ByteArrayOutputStream(string.length)
        val gzip = GZIPOutputStream(bos)
        gzip.write(string.toByteArray())
        gzip.close()
        val compressed = bos.toByteArray()
        bos.close()
        return base64(compressed)
    }
}