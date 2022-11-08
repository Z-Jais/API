package fr.ziedelth.repositories

import fr.ziedelth.entities.Simulcast
import fr.ziedelth.utils.Database
import org.hibernate.Session

class SimulcastRepository(session: () -> Session = { Database.getSession() }) : IRepository<Simulcast>(session)