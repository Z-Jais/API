package fr.ziedelth.repositories

import fr.ziedelth.entities.LangType
import fr.ziedelth.utils.Database
import org.hibernate.Session

class LangTypeRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<LangType>(session)