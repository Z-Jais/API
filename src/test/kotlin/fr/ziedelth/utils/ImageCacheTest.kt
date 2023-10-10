package fr.ziedelth.utils

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ImageCacheTest {

    @Test
    fun getRandomUUID() {
        assertNull(ImageCache.get(UUID.randomUUID()))
    }

    @Test
    fun cache() {
        val uuid = UUID.randomUUID()
        val imageUrl = "https://www.crunchyroll.com/imgsrv/display/thumbnail/480x720/catalog/crunchyroll/26afbe6a1577d35adb996205f563e34b.jpe"
        ImageCache.cache(uuid, imageUrl)

        assertTrue(ImageCache.contains(uuid))
        val encodedImage = ImageCache.get(uuid)
        assertNotNull(encodedImage)
        assertEquals(imageUrl, encodedImage.url)
        assertEquals("webp", encodedImage.type)
    }
}