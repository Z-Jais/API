package fr.ziedelth.controllers

import fr.ziedelth.utils.Database
import fr.ziedelth.utils.ImageCache
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.*

open class IController<T : Serializable>(val prefix: String) {
    val entityClass: Class<T> = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    val entityName: String = entityClass.simpleName

    private fun getAll(): MutableList<T> {
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

    fun getBy(field: String, value: Any?): T? {
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

    fun Route.getAll() {
        get {
            println("GET $prefix")

            try {
                call.respond(this@IController.getAll())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }

    fun isExists(field: String, value: String?): Boolean {
        val session = Database.getSession()
        val query = session.createQuery("SELECT COUNT(*) FROM $entityName WHERE $field = :$field", Long::class.java)
        query.maxResults = 1
        query.setParameter(field, value)
        val list = query.uniqueResult()
        session.close()
        return list > 0
    }

    fun contains(fieldList: String, searchValue: String?): Boolean {
        val session = Database.getSession()
        val query = session.createQuery(
            "SELECT COUNT(*) FROM $entityName JOIN $fieldList l WHERE l = :search",
            Long::class.java
        )
        query.maxResults = 1
        query.setParameter("search", searchValue)
        val list = query.uniqueResult()
        session.close()
        return list > 0
    }

    fun <T : Serializable> justSave(dtoIn: T): T {
        val session = Database.getSession()
        val transaction = session.beginTransaction()

        try {
            val entity = session.merge(dtoIn)
            session.persist(entity)
            transaction.commit()
            return entity
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while saving $prefix : ${e.message}")
            transaction.rollback()
            throw e
        } finally {
            session.close()
        }
    }

    fun Route.getAttachment() {
        get("/attachment/{uuid}") {
            val uuid = UUID.fromString(call.parameters["uuid"]) ?: return@get call.respond(HttpStatusCode.BadRequest)
            println("GET ${prefix}/attachment/$uuid")

            if (!ImageCache.contains(uuid)) {
                call.respond(HttpStatusCode.NoContent)
                return@get
            }

            val image = ImageCache.get(uuid) ?: return@get call.respond(HttpStatusCode.NoContent)
            call.respondBytes(image.bytes, ContentType("image", "webp"))
        }
    }
}
