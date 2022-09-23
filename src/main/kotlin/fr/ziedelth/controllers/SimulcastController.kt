package fr.ziedelth.controllers

import fr.ziedelth.controllers.CountryController.getAll
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.utils.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object SimulcastController : IController<Simulcast>("/simulcasts") {
    fun Routing.getSimulcasts() {
        route(prefix) {
            getAll()

            get("/country/{country}") {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET $prefix/country/$country")
                val session = Database.getSession()

                try {
                    val query = session.createQuery("SELECT DISTINCT simulcasts FROM Anime WHERE country.tag = :tag", Simulcast::class.java)
                    query.setParameter("tag", country)
                    call.respond(query.list())
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                } finally {
                    session.close()
                }
            }
        }
    }

    fun getBy(simulcast: Simulcast): Simulcast {
        val session = Database.getSession()

        try {
            val query = session.createQuery("FROM Simulcast WHERE season = :season AND year = :year", Simulcast::class.java)
            query.setParameter("season", simulcast.season)
            query.setParameter("year", simulcast.year)
            return query.list().firstOrNull() ?: simulcast
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while getting $prefix : ${e.message}")
            throw e
        } finally {
            session.close()
        }
    }
}
