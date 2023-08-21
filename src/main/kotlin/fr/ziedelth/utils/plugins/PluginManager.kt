package fr.ziedelth.utils.plugins

import fr.ziedelth.utils.plugins.events.Event
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import org.pf4j.DefaultPluginManager
import java.io.File

object PluginManager {
    private var defaultPluginManager = DefaultPluginManager(File("data/plugins").toPath())
    val listeners = mutableListOf<Listener>()

    fun loadPlugins() {
        defaultPluginManager.loadPlugins()
        defaultPluginManager.startPlugins()

        defaultPluginManager.plugins.forEach {
            println("Plugin ${it.descriptor.pluginId} v${it.descriptor.version} loaded")
        }
    }

    fun reload() {
        defaultPluginManager.stopPlugins()
        defaultPluginManager.unloadPlugins()
        listeners.clear()
        defaultPluginManager = DefaultPluginManager(File("data/plugins").toPath())
        loadPlugins()
    }

    fun callEvent(event: Event): Int {
        var count = 0

        listeners.forEach { listener ->
            listener::class.java.methods.filter { it.isAnnotationPresent(EventHandler::class.java) && event::class.java == it.parameters[0]?.type }
                .forEach { method ->
                    println("Calling event ${event::class.java.simpleName} on ${listener::class.java.name}")
                    println(method.parameters.map { it.type }.joinToString(" -> "))

                    try {
                        method.invoke(listener, event)
                        count++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }

        return count
    }

    fun registerEvents(vararg listener: Listener) {
        listeners.addAll(listener)
    }
}