package fr.ziedelth.utils

import fr.ziedelth.plugins.session
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
        val transaction = session.beginTransaction()

        try {
            session.createQuery("DELETE FROM Episode").executeUpdate()
            session.createQuery("DELETE FROM Manga").executeUpdate()
            session.createQuery("DELETE FROM News").executeUpdate()
            session.createQuery("DELETE FROM EpisodeType").executeUpdate()
            session.createQuery("DELETE FROM LangType").executeUpdate()
            session.createQuery("DELETE FROM Genre").executeUpdate()
            session.createQuery("DELETE FROM Anime").executeUpdate()
            session.createQuery("DELETE FROM Simulcast").executeUpdate()
            session.createQuery("DELETE FROM Platform").executeUpdate()
            session.createQuery("DELETE FROM Country").executeUpdate()
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        }
    }
}