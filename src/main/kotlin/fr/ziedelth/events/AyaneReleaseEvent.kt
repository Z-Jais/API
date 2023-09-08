package fr.ziedelth.events

import fr.ziedelth.dtos.Ayane
import fr.ziedelth.utils.plugins.events.Event

data class AyaneReleaseEvent(val ayane: Ayane) : Event