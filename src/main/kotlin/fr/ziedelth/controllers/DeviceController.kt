package fr.ziedelth.controllers

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.entities.Device
import fr.ziedelth.entities.Manga
import fr.ziedelth.entities.device_redirections.DeviceEpisodeRedirection
import fr.ziedelth.entities.device_redirections.DeviceMangaRedirection
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.utils.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object DeviceController : IController<Device>("/devices") {
    fun update(device: Device) {
        val session = Database.getSession()
        val transaction = session.beginTransaction()

        try {
            val newDevice = getBy("name", device.name)

            if (newDevice.isNullOrNotValid()) {
                println("Missing parameters")
                println(device)
                return
            }

            newDevice!!.os = device.os
            newDevice.model = device.model
            newDevice.updatedAt = Calendar.getInstance()
            session.update(newDevice)
            transaction.commit()
        } catch (e: Exception) {
            println("Error while updating $entityName")
            println(e)
            transaction.rollback()
            throw e
        } finally {
            session.close()
        }
    }

    fun update(name: String) {
        val session = Database.getSession()
        val transaction = session.beginTransaction()

        try {
            val newDevice = getBy("name", name) ?: return
            newDevice.updatedAt = Calendar.getInstance()
            session.update(newDevice)
            transaction.commit()
        } catch (e: Exception) {
            println("Error while updating $entityName")
            println(e)
            transaction.rollback()
            throw e
        } finally {
            session.close()
        }
    }

    fun Routing.getDevices() {
        route(prefix) {
            create()
            createRedirection()
        }
    }

    private fun Route.create() {
        post {
            try {
                val gson = Gson().fromJson(call.receiveText(), JsonObject::class.java)

                val device = Device(
                    name = gson.get("name")?.asString,
                    os = gson.get("os")?.asString,
                    model = gson.get("model")?.asString
                )

                println("POST $prefix")

                if (device.isNullOrNotValid()) {
                    println("Missing parameters")
                    println(device)
                    call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                    return@post
                }

                if (isExists("name", device.name)) {
                    println("$entityName already exists, updating")

                    try {
                        update(device)
                        call.respond(HttpStatusCode.OK, "$entityName updated")
                    } catch (e: Exception) {
                        println("Error while updating $entityName")
                        println(e)
                        call.respond(HttpStatusCode.InternalServerError, "Error while updating $entityName")
                    }

                    return@post
                }

                val savedDevice = justSave(device)
                call.respond(HttpStatusCode.Created, savedDevice)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }

    private fun Route.createRedirection() {
        route("/redirection") {
            post("/episode") {
                try {
                    val deviceName = call.request.header("Device") ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val episodeId = UUID.fromString(call.request.header("Episode") ?: return@post call.respond(HttpStatusCode.BadRequest))
                    println("POST $prefix/redirection/episode")

                    if (!isExists("name", deviceName)) {
                        println("$entityName doesn't exists")
                        return@post
                    }

                    if (!EpisodeController.isExists("uuid", episodeId)) {
                        println("Episode doesn't exists")
                        return@post
                    }

                    update(deviceName)
                    justSave(
                        DeviceEpisodeRedirection(
                            device = getBy("name", deviceName),
                            episode = EpisodeController.getBy("uuid", episodeId)
                        )
                    )
                    call.respond(HttpStatusCode.Created, "$entityName created")
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                }
            }
            post("/manga") {
                try {
                    val deviceName = call.request.header("Device") ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val mangaId = UUID.fromString(call.request.header("Manga") ?: return@post call.respond(HttpStatusCode.BadRequest))
                    println("POST $prefix/redirection/manga")

                    if (!isExists("name", deviceName)) {
                        println("$entityName doesn't exists")
                        return@post
                    }

                    if (!MangaController.isExists("uuid", mangaId)) {
                        println("Episode doesn't exists")
                        return@post
                    }

                    update(deviceName)
                    justSave(
                        DeviceMangaRedirection(
                            device = getBy("name", deviceName),
                            manga = MangaController.getBy("uuid", mangaId)
                        )
                    )
                    call.respond(HttpStatusCode.Created, "$entityName created")
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                }
            }
        }
    }
}
