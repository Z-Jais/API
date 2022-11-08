package fr.ziedelth.repositories

import fr.ziedelth.utils.Database
import org.hibernate.Session
import java.lang.reflect.ParameterizedType
import java.util.UUID

open class IRepository<T>(val getSession: () -> Session = { Database.getSession() }) {
    private val entityClass: Class<T> = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    private val entityName: String = entityClass.simpleName

    fun find(uuid: UUID): T? {
        val session = getSession.invoke()
        val entity = session.find(entityClass, uuid)
        session.close()
        return entity
    }

    fun getAll(): MutableList<T> {
        val session = getSession.invoke()
        val list = session.createQuery("FROM $entityName", entityClass).list()
        session.close()
        return list
    }

    fun save(entity: T): T {
        val session = getSession.invoke()
        val transaction = session.beginTransaction()

        try {
            session.persist(session.merge(entity))
            transaction.commit()
            return entity
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
}
