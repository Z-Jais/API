package fr.ziedelth

import fr.ziedelth.plugins.configureHTTP
import fr.ziedelth.plugins.configureRouting
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.PerformanceMeter
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    PerformanceMeter.startThread()
    Database.getSessionFactory()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureHTTP()
        configureRouting()
    }.start(wait = true)
}
