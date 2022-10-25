package fr.ziedelth.events

import fr.ziedelth.entities.News
import fr.ziedelth.utils.plugins.events.Event

data class NewsReleaseEvent(val mangas: Collection<News>) : Event