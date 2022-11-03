package fr.ziedelth.utils

import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.GZIPInputStream

object Decoder {
    private fun base64(string: String): ByteArray = Base64.getDecoder().decode(string)

    fun fromGzip(string: String): String {
        val gzip = GZIPInputStream(ByteArrayInputStream(base64(string)))
        val compressed = gzip.readBytes()
        gzip.close()
        return String(compressed)
    }
}