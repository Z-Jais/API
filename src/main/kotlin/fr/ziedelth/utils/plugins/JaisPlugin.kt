package fr.ziedelth.utils.plugins

import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import java.io.File

abstract class JaisPlugin(pluginWrapper: PluginWrapper) : Plugin() {
    private val pluginsDirectory = PluginManager.pluginsDirectory
        get() {
            if (!field.exists()) {
                field.mkdirs()
            }

            return field
        }

    val dataFolder = File(pluginsDirectory, pluginWrapper.pluginId)
}
