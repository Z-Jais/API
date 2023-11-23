package fr.ziedelth.events

import fr.ziedelth.dtos.CalendarDto
import fr.ziedelth.utils.plugins.events.Event

data class CalendarReleaseEvent(val calendarDto: CalendarDto) : Event