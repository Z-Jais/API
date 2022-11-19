package fr.ziedelth.repositories

import fr.ziedelth.entities.Episode
import fr.ziedelth.utils.Database
import org.hibernate.Session

class EpisodeRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<Episode>(session)