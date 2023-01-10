package fr.ziedelth.repositories

import fr.ziedelth.entities.Genre
import org.hibernate.Session

class GenreRepository(session: Session) : AbstractRepository<Genre>(session)