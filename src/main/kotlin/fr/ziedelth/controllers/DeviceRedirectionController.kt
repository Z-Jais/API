package fr.ziedelth.controllers

import fr.ziedelth.entities.Device
import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Manga
import fr.ziedelth.entities.device_redirections.DeviceEpisodeRedirection
import fr.ziedelth.entities.device_redirections.DeviceMangaRedirection
import fr.ziedelth.utils.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object DeviceRedirectionController : IController<Device>("/devices/redirection") {
    fun Route.getRedirection() {
        route(prefix) {
            episode()
            manga()
        }
    }

    private fun getEpisodeRedirection(device: Device, episode: Episode): DeviceEpisodeRedirection? {
        val session = Database.getSession()

        try {
            val query = session.createQuery(
                "FROM DeviceEpisodeRedirection WHERE device = :device AND episode = :episode",
                DeviceEpisodeRedirection::class.java
            )
            query.maxResults = 1
            query.setParameter("device", device)
            query.setParameter("episode", episode)
            return query.uniqueResult()
        } catch (e: Exception) {
            return null
        } finally {
            session.close()
        }
    }

    private fun Route.episode() {
        post("/episode") {
            try {
                val deviceName = call.request.header("Device") ?: return@post call.respond(HttpStatusCode.BadRequest)
                val episodeId = UUID.fromString(
                    call.request.header("Episode") ?: return@post call.respond(
                        HttpStatusCode.BadRequest
                    )
                )
                println("POST $prefix/episode")
                val device = DeviceController.getBy("name", deviceName)
                val episode = EpisodeController.getBy("id", episodeId)

                if (device == null || episode == null) {
                    println("Missing parameters")
                    return@post call.respond(HttpStatusCode.BadRequest)
                }

                DeviceController.update(deviceName, device)

                val redirection = getEpisodeRedirection(device, episode)

                if (redirection != null) {
                    redirection.timestamp = Calendar.getInstance()
                    justUpdate(redirection)
                } else {
                    justSave(DeviceEpisodeRedirection(device = device, episode = episode))
                }

                call.respond(HttpStatusCode.Created, "$entityName created")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }

    private fun getMangaRedirection(device: Device, manga: Manga): DeviceMangaRedirection? {
        val session = Database.getSession()

        try {
            val query = session.createQuery(
                "FROM DeviceMangaRedirection WHERE device = :device AND manga = :manga",
                DeviceMangaRedirection::class.java
            )
            query.maxResults = 1
            query.setParameter("device", device)
            query.setParameter("manga", manga)
            return query.uniqueResult()
        } catch (e: Exception) {
            return null
        } finally {
            session.close()
        }
    }

    private fun Route.manga() {
        post("/manga") {
            try {
                val deviceName = call.request.header("Device") ?: return@post call.respond(HttpStatusCode.BadRequest)
                val mangaId = UUID.fromString(
                    call.request.header("Manga") ?: return@post call.respond(
                        HttpStatusCode.BadRequest
                    )
                )
                println("POST $prefix/manga")
                val device = DeviceController.getBy("name", deviceName)
                val manga = MangaController.getBy("uuid", mangaId)

                if (device == null || manga == null) {
                    println("Missing parameters")
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                DeviceController.update(deviceName, device)

                val redirection = getMangaRedirection(device, manga)

                if (redirection != null) {
                    redirection.timestamp = Calendar.getInstance()
                    justUpdate(redirection)
                } else {
                    justSave(DeviceMangaRedirection(device = device, manga = manga))
                }

                call.respond(HttpStatusCode.Created, "$entityName created")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}