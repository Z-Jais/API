package fr.ziedelth.repositories

import fr.ziedelth.entities.Platform
import org.hibernate.Session

class PlatformRepository(session: Session) : AbstractRepository<Platform>(session)