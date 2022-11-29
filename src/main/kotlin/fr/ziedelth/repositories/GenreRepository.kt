package fr.ziedelth.repositories

import fr.ziedelth.entities.Genre
import fr.ziedelth.utils.Database
import org.hibernate.Session

class GenreRepository(session: () -> Session = { Database.getSession() }) : AbstractRepository<Genre>(session)