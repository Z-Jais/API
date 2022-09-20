package fr.ziedelth.controllers

import fr.ziedelth.utils.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.hibernate.Session
import java.io.Serializable
import java.lang.reflect.ParameterizedType

open class IController<T : Serializable>(val prefix: String) {
    val entityClass: Class<T> = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    val entityName: String = entityClass.simpleName

    fun Route.get() {
        get {
            println("GET $prefix")
            val session = Database.getSession()

            try {
                val query = session.createQuery("FROM $entityName", entityClass)
                val result = query.list()
                call.respond(result)
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error while getting $prefix : ${e.message}")
                call.respond(e)
            } finally {
                session.close()
            }
        }
    }

    fun isExists(session: Session, field: String, value: String): Boolean {
        val query = session.createQuery("FROM $entityName WHERE $field = :$field", entityClass)
        query.setParameter(field, value)
        return query.list().isNotEmpty()
    }

    suspend inline fun <reified T : Serializable> PipelineContext<Unit, ApplicationCall>.save(session: Session, dtoIn: T) {
        try {
            session.beginTransaction()
            val entity = session.merge(dtoIn)
            session.persist(entity)
            session.transaction.commit()
            call.respond(entity)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while posting $prefix : ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, e)
        } finally {
            session.close()
        }
    }
}