package fr.ziedelth.utils.plugins

import fr.ziedelth.utils.plugins.events.Event
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import org.pf4j.DefaultPluginManager
import java.io.File

object PluginManager {
    private val defaultPluginManager = DefaultPluginManager(File("plugins").toPath())
    val listeners = mutableListOf<Listener>()

    fun loadPlugins() {
        defaultPluginManager.loadPlugins()
        defaultPluginManager.startPlugins()
    }

    fun callEvent(event: Event): Int {
        var count = 0

        listeners.forEach { listener ->
            listener::class.java.methods.filter { it.isAnnotationPresent(EventHandler::class.java) }.forEach { method ->
                method.invoke(listener, event)
                count++
            }
        }

        return count
    }

    fun registerEvents(vararg listener: Listener) {
        listeners.addAll(listener)
    }
}