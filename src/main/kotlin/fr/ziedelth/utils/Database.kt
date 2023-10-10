package fr.ziedelth.utils

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.reflections.Reflections
import java.io.File
import java.io.Serializable
import kotlin.system.exitProcess

open class Database {
    private var sessionFactory: SessionFactory

    constructor(file: File) {
        try {
            if (!file.exists()) {
                Logger.warning("hibernate.cfg.xml not found")
                exitProcess(1)
            }

            Configuration().let { configuration ->
                getEntities().forEach { configuration.addAnnotatedClass(it) }
                configuration.configure(file)

                val url: String? = System.getenv("DATABASE_URL")
                val username: String? = System.getenv("DATABASE_USERNAME")
                val password: String? = System.getenv("DATABASE_PASSWORD")

                if (url?.isNotBlank() == true) {
                    Logger.config("Bypassing hibernate.cfg.xml with system environment variable DATABASE_URL")
                    configuration.setProperty("hibernate.connection.url", url)
                }

                if (username?.isNotBlank() == true) {
                    Logger.config("Bypassing hibernate.cfg.xml with system environment variable DATABASE_USERNAME")
                    configuration.setProperty("hibernate.connection.username", username)
                }

                if (password?.isNotBlank() == true) {
                    Logger.config("Bypassing hibernate.cfg.xml with system environment variable DATABASE_PASSWORD")
                    configuration.setProperty("hibernate.connection.password", password)
                }

                sessionFactory = configuration.buildSessionFactory(
                    StandardServiceRegistryBuilder().applySettings(configuration.properties).build()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(1)
        }
    }

    constructor() : this(File("data/hibernate.cfg.xml"))

    protected fun getEntities(): MutableSet<Class<out Serializable>> =
        Reflections("fr.ziedelth.entities").getSubTypesOf(Serializable::class.java)

    private fun getCurrentSession(): Session = sessionFactory.openSession()

    private fun openReadOnlySession(): Session {
        val session = sessionFactory.openSession()
        session.isDefaultReadOnly = true
        return session
    }

    fun <T> inTransaction(block: (Session) -> T): T {
        getCurrentSession().use { session ->
            val transaction = session.beginTransaction()

            try {
                val result = block(session)
                transaction.commit()
                return result
            } catch (e: Exception) {
                transaction.rollback()
                throw e
            } finally {
                session.close()
            }
        }
    }

    fun <T> inReadOnlyTransaction(block: (Session) -> T): T {
        openReadOnlySession().use { session ->
            try {
                return block(session)
            } catch (e: Exception) {
                throw e
            } finally {
                session.close()
            }
        }
    }
}
