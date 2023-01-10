package fr.ziedelth.repositories

import fr.ziedelth.entities.EpisodeType
import org.hibernate.Session

class EpisodeTypeRepository(session: Session) : AbstractRepository<EpisodeType>(session)