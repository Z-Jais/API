package fr.ziedelth.repositories

import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.utils.Database
import org.hibernate.Session

class EpisodeTypeRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<EpisodeType>(session)