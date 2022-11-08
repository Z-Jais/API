package fr.ziedelth.repositories

import fr.ziedelth.entities.Anime
import fr.ziedelth.utils.Database
import org.hibernate.Session

class AnimeRepository(session: () -> Session = { Database.getSession() }) : IRepository<Anime>(session)