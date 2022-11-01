package fr.ziedelth.listeners

import fr.ziedelth.utils.plugins.PluginManager

class ListenerManager {
    init {
        PluginManager.registerEvents(EpisodesRelease())
    }
}