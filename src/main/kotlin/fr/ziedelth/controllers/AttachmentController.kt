package fr.ziedelth.controllers

import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.Logger
import fr.ziedelth.utils.routes.*
import fr.ziedelth.utils.routes.method.Get
import io.ktor.http.*
import java.io.Serializable
import java.util.*

@IgnorePath
open class AttachmentController<T : Serializable>(override val prefix: String) : AbstractController<T>(prefix) {
    @Path("/attachment/{uuid}")
    @Get
    @Cached(172800)
    fun attachmentByUUID(uuid: UUID): Response {
        if (!ImageCache.contains(uuid)) {
            Logger.warning("Attachment $uuid not found")
            return Response(HttpStatusCode.NoContent)
        }

        val image = ImageCache.get(uuid)!!
        Logger.config("Attachment $uuid found (${image.bytes.size} bytes)")
        return ResponseMultipart(image.bytes, ContentType("image", image.type))
    }
}