package fr.ziedelth.events

import fr.ziedelth.entities.Manga
import fr.ziedelth.utils.plugins.events.Event

data class MangasReleaseEvent(val mangas: Collection<Manga>) : Event