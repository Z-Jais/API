package fr.ziedelth.utils

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.reflections.Reflections
import java.io.File
import java.io.Serializable
import kotlin.system.exitProcess

object Database {
    private var sessionFactory: SessionFactory

    init {
        try {
            val file = File("hibernate.cfg.xml")

            if (!file.exists()) {
                println("hibernate.cfg.xml not found")
                exitProcess(1)
            }

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

    fun getEntities(): MutableSet<Class<out Serializable>> =
        Reflections("fr.ziedelth.entities").getSubTypesOf(Serializable::class.java)

    fun getSessionFactory() = sessionFactory
    fun getSession(): Session = sessionFactory.openSession()
}