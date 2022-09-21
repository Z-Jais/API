package fr.ziedelth.controllers

import fr.ziedelth.utils.Database
import fr.ziedelth.utils.PerformanceMeter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.UUID

open class IController<T : Serializable>(val prefix: String) {
    val entityClass: Class<T> = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    val entityName: String = entityClass.simpleName

    fun getAll(): MutableList<T> {
        val session = Database.getSession()

        try {
            val query = session.createQuery("FROM $entityName", entityClass)
            return query.list()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while getting $prefix : ${e.message}")
            throw e
        } finally {
            session.close()
        }
    }

    fun getBy(field: String, value: Any): T? {
        val session = Database.getSession()

        try {
            val query = session.createQuery("FROM $entityName WHERE $field = :$field", entityClass)
            query.setParameter(field, value)
            return query.list().firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while getting $prefix : ${e.message}")
            throw e
        } finally {
            session.close()
        }
    }

    fun getByUuid(uuid: UUID): T? = getBy("uuid", uuid)

    fun Route.getAll() {
        get {
            PerformanceMeter.request++
            println("GET $prefix")

            try {
                call.respond(this@IController.getAll())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }

    fun isExists(field: String, value: String): Boolean {
        val session = Database.getSession()
        val query = session.createQuery("FROM $entityName WHERE $field = :$field", entityClass)
        query.setParameter(field, value)
        val list = query.list()
        session.close()
        return list.isNotEmpty()
    }

    fun contains(fieldList: String, searchValue: String): Boolean {
        val session = Database.getSession()
        val query = session.createQuery("FROM $entityName JOIN $fieldList l WHERE l = :search", entityClass)
        query.setParameter("search", searchValue)
        val list = query.list()
        session.close()
        return list.isNotEmpty()
    }

    suspend inline fun <reified T : Serializable> PipelineContext<Unit, ApplicationCall>.save(dtoIn: T) {
        val session = Database.getSession()

        try {
            session.beginTransaction()
            val entity = session.merge(dtoIn)
            session.persist(entity)
            session.transaction.commit()
            call.respond(entity)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while saving $prefix : ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, e)
        } finally {
            session.close()
        }
    }
}
