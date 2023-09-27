package fr.ziedelth.repositories

import com.google.inject.Inject
import fr.ziedelth.utils.Database
import java.lang.reflect.ParameterizedType
import java.util.*

open class AbstractRepository<T> {
    @Inject
    protected lateinit var database: Database

    private val entityClass: Class<T> =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    private val entityName: String = entityClass.simpleName

    fun find(uuid: UUID): T? {
        return database.inTransaction { database.fullInitialize(it.find(entityClass, uuid)) }
    }

    fun exists(field: String, value: Any?): Boolean {
        return database.inTransaction {
            val query = it.createQuery("SELECT uuid FROM $entityName WHERE $field = :$field", UUID::class.java)
            query.maxResults = 1
            query.setParameter(field, value)
            query.uniqueResult()
        } != null
    }

    fun findAll(uuids: List<UUID>): List<T> {
        return database.inTransaction {
            it.createQuery("FROM $entityName WHERE uuid IN :uuids", entityClass)
                .setParameter("uuids", uuids)
                .resultList
        }
    }

    fun getAll(): MutableList<T> {
        return database.inTransaction {
            database.fullInitialize(
                it.createQuery("FROM $entityName", entityClass).list()
            )
        }
    }

    fun getAllBy(field: String, value: Any?): MutableList<T> {
        return database.inTransaction {
            val query = it.createQuery("FROM $entityName WHERE $field = :value", entityClass)
            query.setParameter("value", value)
            query.list()
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

    fun <A> getByPage(
        clazz: Class<A>,
        page: Int,
        limit: Int,
        queryRaw: String,
        vararg pair: Pair<String, Any>?
    ): List<A> {
        return database.inTransaction {
            val query = it.createQuery(queryRaw, clazz)
            pair.forEach { param -> if (param != null) query.setParameter(param.first, param.second) }
            query.firstResult = (limit * page) - limit
            query.maxResults = limit
            database.fullInitialize(query.list())
        }
    }

    fun getByPage(page: Int, limit: Int, queryRaw: String, vararg pair: Pair<String, Any>): List<T> {
        return getByPage(entityClass, page, limit, queryRaw, *pair)
    }
}
