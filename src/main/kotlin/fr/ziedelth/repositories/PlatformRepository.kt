package fr.ziedelth.repositories

import fr.ziedelth.entities.Platform
import fr.ziedelth.utils.Database
import org.hibernate.Session

class PlatformRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<Platform>(session)