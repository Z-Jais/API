package fr.ziedelth.events

import fr.ziedelth.dtos.AyaneDto
import fr.ziedelth.utils.plugins.events.Event

data class AyaneReleaseEvent(val ayane: AyaneDto) : Event