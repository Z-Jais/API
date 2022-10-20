package fr.ziedelth.controllers

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.entities.Device
import fr.ziedelth.entities.isNullOrNotValid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object DeviceController : IController<Device>("/devices") {
    private fun update(device: Device) {
        val newDevice = getBy("name", device.name)

        if (newDevice.isNullOrNotValid()) {
            println("Missing parameters")
            println(device)
            return
        }

        newDevice!!.os = device.os
        newDevice.model = device.model
        newDevice.updatedAt = Calendar.getInstance()
        justUpdate(newDevice)
    }

    fun update(name: String, device: Device? = null) {
        val newDevice = device ?: getBy("name", name) ?: return
        newDevice.updatedAt = Calendar.getInstance()
        justUpdate(newDevice)
    }

    fun Routing.getDevices() {
        route(prefix) {
            create()
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
}
