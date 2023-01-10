package fr.ziedelth.repositories

import org.hibernate.Session
import org.hibernate.jpa.AvailableHints
import java.lang.reflect.ParameterizedType
import java.util.*

open class AbstractRepository<T>(val session: Session) {
    private val entityClass: Class<T> =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    private val entityName: String = entityClass.simpleName

    fun find(uuid: UUID): T? {
        return session.find(entityClass, uuid)
    }

    fun exists(field: String, value: Any?): Boolean {
        val query = session.createQuery("SELECT uuid FROM $entityName WHERE $field = :$field", UUID::class.java)
        query.maxResults = 1
        query.setParameter(field, value)
        query.setHint(AvailableHints.HINT_READ_ONLY, true)
        return query.uniqueResult() != null
    }

    fun findAll(uuids: List<UUID>): List<T> {
        return session.createQuery("FROM $entityName WHERE uuid IN :uuids", entityClass)
            .setParameter("uuids", uuids)
            .setHint(AvailableHints.HINT_READ_ONLY, true)
            .resultList
    }

    fun getAll(): MutableList<T> {
        return session.createQuery("FROM $entityName", entityClass)
            .setHint(AvailableHints.HINT_READ_ONLY, true)
            .resultList
    }

    fun getAllBy(field: String, value: Any?): MutableList<T> {
        val query = session.createQuery("FROM $entityName WHERE $field = :value", entityClass)
        query.setParameter("value", value)
        query.setHint(AvailableHints.HINT_READ_ONLY, true)
        return query.resultList
    }

    fun save(entity: T): T {
        val transaction = session.beginTransaction()

        try {
            val mergedEntity = session.merge(entity)
            session.persist(mergedEntity)
            transaction.commit()
            return mergedEntity
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        }
    }

    fun saveAll(entities: List<T>): List<T> {
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
        }
    }

    fun delete(entity: T) {
        val transaction = session.beginTransaction()

        try {
            session.remove(session.merge(entity))
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        }
    }

    fun deleteAll(entities: List<T>) {
        val transaction = session.beginTransaction()

        try {
            entities.forEach { session.remove(session.merge(it)) }
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        }
    }

    fun getByPage(page: Int, limit: Int, queryRaw: String, vararg pair: Pair<String, Any>): List<T> {
        val query = session.createQuery(queryRaw, entityClass)
        pair.forEach { query.setParameter(it.first, it.second) }
        query.firstResult = (limit * page) - limit
        query.maxResults = limit
        query.setHint(AvailableHints.HINT_READ_ONLY, true)
        return query.list()
    }
}
