package fr.ziedelth.repositories

import fr.ziedelth.utils.Database
import org.hibernate.Session
import java.lang.reflect.ParameterizedType
import java.util.*

open class AbstractRepository<T>(val getSession: () -> Session = { Database.getSession() }) {
    private val entityClass: Class<T> =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    private val entityName: String = entityClass.simpleName

    fun find(uuid: UUID): T? {
        val session = getSession.invoke()
        val entity = session.find(entityClass, uuid)
        session.close()
        return entity
    }

    fun exists(field: String, value: Any?): Boolean {
        val session = getSession.invoke()
        val query = session.createQuery("SELECT uuid FROM $entityName WHERE $field = :$field", UUID::class.java)
        query.maxResults = 1
        query.setParameter(field, value)
        val uuid = query.uniqueResult()
        session.close()
        return uuid != null
    }

    fun findAll(uuids: List<UUID>): List<T> {
        val session = getSession.invoke()
        val entities = session.createQuery("FROM $entityName WHERE uuid IN :uuids", entityClass)
            .setParameter("uuids", uuids)
            .resultList
        session.close()
        return entities
    }

    fun getAll(): MutableList<T> {
        val session = getSession.invoke()
        val list = session.createQuery("FROM $entityName", entityClass).list()
        session.close()
        return list
    }

    fun getAllBy(field: String, value: Any?): MutableList<T> {
        val session = getSession.invoke()
        val query = session.createQuery("FROM $entityName WHERE $field = :value", entityClass)
        query.setParameter("value", value)
        val list = query.list()
        session.close()
        return list
    }

    fun save(entity: T): T {
        val session = getSession.invoke()
        val transaction = session.beginTransaction()

        try {
            val mergedEntity = session.merge(entity)
            session.persist(mergedEntity)
            transaction.commit()
            return mergedEntity
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        } finally {
            session.close()
        }
    }

    fun saveAll(entities: List<T>): List<T> {
        val session = getSession.invoke()
        val transaction = session.beginTransaction()

        try {
            val mergedEntities = entities.map {
                val mergedEntity = session.merge(it)
                session.persist(mergedEntity)
                mergedEntity
            }

            transaction.commit()
            return mergedEntities
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        } finally {
            session.close()
        }
    }

    fun delete(entity: T) {
        val session = getSession.invoke()
        val transaction = session.beginTransaction()

        try {
            session.remove(session.merge(entity))
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        } finally {
            session.close()
        }
    }

    fun deleteAll(entities: List<T>) {
        val session = getSession.invoke()
        val transaction = session.beginTransaction()

        try {
            entities.forEach { session.remove(session.merge(it)) }
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        } finally {
            session.close()
        }
    }
}
