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
    private val entityClass: Class<T> =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    val entityName: String = entityClass.simpleName
    val uuidRequest: UUID = UUID.randomUUID()

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

    fun getAllBy(field: String, value: Any?): MutableList<T> {
        val session = Database.getSession()

        try {
            val query = session.createQuery(
                "FROM $entityName WHERE $field = :${field.filter { it.isLetterOrDigit() }.trim()}",
                entityClass
            )
            query.setParameter(field.filter { it.isLetterOrDigit() }.trim(), value)
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
            query.maxResults = 1
            query.setParameter(field, value)
            return query.uniqueResult()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while getting $prefix : ${e.message}")
            throw e
        } finally {
            session.close()
        }
    }

    fun isExists(field: String, value: Any?): Boolean {
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

    fun <T : Serializable> justUpdate(dtoIn: T) {
        val session = Database.getSession()
        val transaction = session.beginTransaction()

        try {
            session.update(dtoIn)
            transaction.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while updating $prefix : ${e.message}")
            transaction.rollback()
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

    fun Route.getAttachment() {
        get("/attachment/{uuid}") {
            val string = call.parameters["uuid"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val uuidRegex =
                "^[0-9(a-f|A-F)]{8}-[0-9(a-f|A-F)]{4}-4[0-9(a-f|A-F)]{3}-[89ab][0-9(a-f|A-F)]{3}-[0-9(a-f|A-F)]{12}\$".toRegex()

            if (!uuidRegex.matches(string)) {
                println("GET $prefix/attachment/$string : Invalid UUID")
                return@get call.respond(HttpStatusCode.BadRequest)
            }

            val uuid = UUID.fromString(string)
            println("GET ${prefix}/attachment/$uuid")

            if (!ImageCache.contains(uuid)) {
                println("Attachment $uuid not found")
                call.respond(HttpStatusCode.NoContent)
                return@get
            }

            val image = ImageCache.get(uuid) ?: run {
                println("Attachment $uuid not found")
                return@get call.respond(HttpStatusCode.NoContent)
            }

            println("Attachment $uuid found (${image.bytes.size} bytes)")
            call.respondBytes(image.bytes, ContentType("image", "webp"))
        }
    }
}
