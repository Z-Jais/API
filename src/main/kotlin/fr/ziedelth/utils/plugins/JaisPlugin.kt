package fr.ziedelth.utils.plugins

import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import java.io.File

abstract class JaisPlugin(pluginWrapper: PluginWrapper) : Plugin(pluginWrapper) {
    private val pluginsFolder = File("plugins")
        get() {
            if (!field.exists()) {
                field.mkdirs()
            }

            return field
        }

    val dataFolder = File(pluginsFolder, pluginWrapper.pluginId)
}
