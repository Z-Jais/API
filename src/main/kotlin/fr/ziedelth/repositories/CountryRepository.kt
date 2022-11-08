package fr.ziedelth.repositories

import fr.ziedelth.entities.Country
import fr.ziedelth.utils.Database
import org.hibernate.Session

class CountryRepository(session: () -> Session = { Database.getSession() }) : IRepository<Country>(session)