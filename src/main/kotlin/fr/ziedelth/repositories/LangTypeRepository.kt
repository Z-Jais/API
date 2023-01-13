package fr.ziedelth.repositories

import fr.ziedelth.entities.LangType
import org.hibernate.Session

class LangTypeRepository(session: Session) : AbstractRepository<LangType>(session)