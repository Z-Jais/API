package fr.ziedelth.controllers

import fr.ziedelth.entities.Anime
import fr.ziedelth.utils.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object DiaryController : IController<Anime>("/diary") {
    fun Routing.getDiary() {
        route(prefix) {
            get("/country/{country}/day/{day}") {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val day = call.parameters["day"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET $prefix/country/$country/day/$day")
                val session = Database.getSession()

                try {
                    val query = session.createQuery(
                        "SELECT anime FROM Episode episode WHERE episode.anime.country.tag = :tag AND current_date - to_date(episode.releaseDate, 'YYYY-MM-DDTHH:MI:SS') <= 7 AND FUNCTION('date_part', 'dow', to_date(episode.releaseDate, 'YYYY-MM-DDTHH:MI:SS')) = :day ORDER BY episode.releaseDate DESC",
                        entityClass
                    )
                    query.setParameter("tag", country)
                    query.setParameter("day", day)
                    val list = query.list()?.distinctBy { it.uuid }
                    call.respond(list ?: HttpStatusCode.NotFound)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                } finally {
                    session.close()
                }
            }
        }
    }
}
