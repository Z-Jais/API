package fr.ziedelth.repositories

import com.google.inject.Inject
import fr.ziedelth.utils.Database
import java.lang.reflect.ParameterizedType
import java.util.*

open class AbstractRepository<T> {
    @Inject
    protected lateinit var database: Database

    private val entityClass: Class<T> =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T> // NOSONAR
    private val entityName: String = entityClass.simpleName

    fun find(uuid: UUID): T? {
        return database.inReadOnlyTransaction { database.fullInitialize(it.find(entityClass, uuid)) }
    }

    fun exists(field: String, value: Any?): Boolean {
        return database.inReadOnlyTransaction {
            val query = it.createQuery("SELECT uuid FROM $entityName WHERE $field = :$field", UUID::class.java)
            query.maxResults = 1
            query.setParameter(field, value)
            query.uniqueResult()
        } != null
    }

    fun findAll(uuids: Collection<UUID>): List<T> {
        return database.inReadOnlyTransaction {
            database.fullInitialize(
                it.createQuery("FROM $entityName WHERE uuid IN :uuids", entityClass)
                .setParameter("uuids", uuids)
                    .resultList
            )
        }
    }

    fun getAll(): MutableList<T> {
        return database.inReadOnlyTransaction { database.fullInitialize(it.createQuery("FROM $entityName", entityClass).resultList) }
    }

    fun getAllBy(field: String, value: Any?): MutableList<T> {
        return database.inReadOnlyTransaction {
            val query = it.createQuery("FROM $entityName WHERE $field = :value", entityClass)
            query.setParameter("value", value)
            database.fullInitialize(query.resultList)
        }
    }

    fun save(entity: T): T {
        return database.inTransaction {
            val mergedEntity = it.merge(entity)
            it.persist(mergedEntity)
            mergedEntity
        }
    }

    fun saveAll(entities: List<T>): List<T> {
        return database.inTransaction {
            entities.map { entity ->
                val mergedEntity = it.merge(entity)
                it.persist(mergedEntity)
                mergedEntity
            }
        }
    }

    fun delete(entity: T) {
        return database.inTransaction { it.remove(it.merge(entity)) }
    }

    fun deleteAll(entities: List<T>) {
        return database.inTransaction {
            entities.forEach { entity -> it.remove(it.merge(entity)) }
        }
    }

    fun merge(entity: T): T {
        return database.inTransaction { it.merge(entity) }
    }

    fun <A> getByPage(
        clazz: Class<A>,
        page: Int,
        limit: Int,
        queryRaw: String,
        vararg pair: Pair<String, Any>?
    ): List<A> {
        return database.inReadOnlyTransaction {
            val query = it.createQuery(queryRaw, clazz)
            pair.forEach { param -> if (param != null) query.setParameter(param.first, param.second) }
            query.firstResult = (limit * page) - limit
            query.maxResults = limit
            database.fullInitialize(query.resultList)
        }
    }

    fun getByPage(page: Int, limit: Int, queryRaw: String, vararg pair: Pair<String, Any>): List<T> {
        return getByPage(entityClass, page, limit, queryRaw, *pair)
    }
}
