package fr.ziedelth.utils.routes

import io.ktor.http.*

open class Response(
    val status: HttpStatusCode = HttpStatusCode.OK,
    val data: Any? = null,
) {
    companion object {
        fun ok(data: Any?): Response = Response(HttpStatusCode.OK, data)
        fun created(data: Any?): Response = Response(HttpStatusCode.Created, data)
    }
}

open class ResponseMultipart(
    val image: ByteArray,
    val contentType: ContentType,
) : Response()