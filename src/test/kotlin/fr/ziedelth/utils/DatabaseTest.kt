package fr.ziedelth.utils

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.reflections.Reflections
import java.io.File
import java.io.Serializable
import kotlin.system.exitProcess

object DatabaseTest {
    private var sessionFactory: SessionFactory

    init {
        try {
            val file = File(
                javaClass.classLoader.getResource("hibernate.cfg.xml")?.file
                    ?: throw Exception("hibernate.cfg.xml not found")
            )

            Configuration().let { configuration ->
                getEntities().forEach { configuration.addAnnotatedClass(it) }

                configuration.configure(file)
                sessionFactory = configuration.buildSessionFactory(
                    StandardServiceRegistryBuilder().applySettings(configuration.properties).build()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(1)
        }
    }

    private fun getEntities(): MutableSet<Class<out Serializable>> =
        Reflections("fr.ziedelth.entities").getSubTypesOf(Serializable::class.java)

    fun getSession(): Session = sessionFactory.openSession()

    fun clean() {
        val session = getSession()
        val transaction = session.beginTransaction()

        try {
            getEntities().forEach { session.createQuery("DELETE FROM ${it.simpleName}").executeUpdate() }
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        } finally {
            session.close()
        }
    }
}