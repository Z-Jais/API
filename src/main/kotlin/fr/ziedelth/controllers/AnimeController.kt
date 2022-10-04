package fr.ziedelth.controllers

import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.utils.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

object AnimeController : IController<Anime>("/animes") {
    fun Routing.getAnimes() {
        route(prefix) {
            getAll()
            search()
            getWithPage()
            getAttachment()
            create()
        }
    }

    private fun Route.search() {
        route("/country/{country}/search") {
            get("/hash/{hash}") {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val hash = call.parameters["hash"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET $prefix/country/$country/search/hash/$hash")
                val session = Database.getSession()

                try {
                    val query = session.createQuery(
                        "FROM Anime a JOIN a.hashes h WHERE a.country.tag = :tag AND h = :hash",
                        Anime::class.java
                    )
                    query.setParameter("tag", country)
                    query.setParameter("hash", hash)
                    call.respond(query.list().firstOrNull() ?: HttpStatusCode.NotFound)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                } finally {
                    session.close()
                }
            }

            get("/name/{name}") {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val name = call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET $prefix/country/$country/search/name/$name")
                val session = Database.getSession()

                try {
                    val query = session.createQuery(
                        "FROM Anime a WHERE a.country.tag = :tag AND LOWER(name) LIKE CONCAT('%', :name, '%') ",
                        Anime::class.java
                    )
                    query.setParameter("tag", country)
                    query.setParameter("name", name.lowercase())
                    call.respond(query.list() ?: HttpStatusCode.NotFound)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                } finally {
                    session.close()
                }
            }
        }
    }

    private fun Route.getWithPage() {
        get("/country/{country}/simulcast/{simulcast}/page/{page}/limit/{limit}") {
            val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val simulcast = call.parameters["simulcast"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = call.parameters["page"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val limit = call.parameters["limit"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            println("GET $prefix/country/$country/simulcast/$simulcast/page/$page/limit/$limit")
            val session = Database.getSession()

            try {
                val query = session.createQuery(
                    "FROM Anime a JOIN a.simulcasts s WHERE a.country.tag = :tag AND s.uuid = :simulcast ORDER BY a.name",
                    Anime::class.java
                )
                query.setParameter("tag", country)
                query.setParameter("simulcast", UUID.fromString(simulcast))
                query.firstResult = (limit * page) - limit
                query.maxResults = limit
                call.respond(query.list())
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            } finally {
                session.close()
            }
        }
    }

    private fun toByteArray(image: BufferedImage): ByteArray {
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        baos.flush()
        val imageInByte = baos.toByteArray()
        baos.close()
        return imageInByte
    }

    private fun getImage(
        uuid: String,
        imageCache: MutableMap<String, Pair<ByteArray, ContentType>>
    ): Pair<ByteArray, ContentType> {
        val session = Database.getSession()

        try {
            val query = session.createQuery("SELECT image FROM Anime WHERE uuid = :uuid", String::class.java)
            query.setParameter("uuid", UUID.fromString(uuid))
            val imageUrl = query.uniqueResult() ?: throw Exception("Image not found")

            val image1 = ImageIO.read(URL(imageUrl))
            val imageType = imageUrl.substring(imageUrl.lastIndexOf(".") + 1)
            val imageBytes = toByteArray(image1)
            val pair = imageBytes to ContentType("image", imageType)
            imageCache[uuid] = pair
            return pair
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            session.close()
        }
    }

    private fun Route.getAttachment() {
        val imageCache = mutableMapOf<String, Pair<ByteArray, ContentType>>()

        get("/attachment/{uuid}") {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            println("GET ${prefix}/attachment/$uuid")
            val image = imageCache[uuid] ?: run { getImage(uuid, imageCache) }
            call.respondBytes(image.first, image.second)
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val anime = call.receive<Anime>()

                anime.country = CountryController.getBy("uuid", anime.country?.uuid) ?: return@post run {
                    println("Country not found")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Country not found"
                    )
                }

                if (anime.isNullOrNotValid()) {
                    println("Missing parameters")
                    println(anime)
                    call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                    return@post
                }

                if (isExists("name", anime.name)) {
                    println("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                val hash = anime.hash()
                if (contains("hashes", hash)) {
                    println("$entityName already exists")
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                if (!(anime.hashes.contains(hash))) {
                    anime.hashes.add(hash!!)
                }

                call.respond(HttpStatusCode.Created, justSave(anime))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}
