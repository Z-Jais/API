package fr.ziedelth.utils.plugins

import fr.ziedelth.events.ExampleEvent
import fr.ziedelth.listeners.ListenerManager
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PluginManagerTest {
    @Test
    fun onTestListener() {
        ListenerManager()

        PluginManager.registerEvents(ExampleListener())
        assertEquals(2, PluginManager.listeners.size)

        val calls = PluginManager.callEvent(ExampleEvent("Hello World!"))
        assertEquals(1, calls)
    }

    class ExampleListener : Listener {
        @EventHandler
        fun onTestListener(event: ExampleEvent) {
            println(event.message)
        }
    }
}