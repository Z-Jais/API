package fr.ziedelth.repositories

import fr.ziedelth.entities.Manga
import fr.ziedelth.utils.Database
import org.hibernate.Session

class MangaRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<Manga>(session)