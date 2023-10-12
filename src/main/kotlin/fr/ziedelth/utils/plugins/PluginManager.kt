package fr.ziedelth.utils.plugins

import fr.ziedelth.utils.Logger
import fr.ziedelth.utils.plugins.events.Event
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import org.pf4j.DefaultPluginManager
import java.io.File

object PluginManager {
    val pluginsDirectory = File("data/plugins")
    private var defaultPluginManager = DefaultPluginManager(pluginsDirectory.toPath())
    val listeners = mutableListOf<Listener>()

    fun loadPlugins() {
        defaultPluginManager.loadPlugins()
        defaultPluginManager.startPlugins()

        defaultPluginManager.plugins.forEach {
            Logger.info("Plugin ${it.descriptor.pluginId} v${it.descriptor.version} loaded")
        }
    }

    fun reload() {
        defaultPluginManager.stopPlugins()
        defaultPluginManager.unloadPlugins()
        listeners.clear()
        defaultPluginManager = DefaultPluginManager(pluginsDirectory.toPath())
        loadPlugins()
    }

    fun callEvent(event: Event): Int {
        var count = 0

        listeners.forEach { listener ->
            listener::class.java.methods.filter { it.isAnnotationPresent(EventHandler::class.java) && event::class.java == it.parameters[0]?.type }
                .forEach { method ->
                    Logger.info("Calling event ${event::class.java.simpleName} on ${listener::class.java.name}")
                    Logger.info(method.parameters.map { it.type }.joinToString(" -> "))

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