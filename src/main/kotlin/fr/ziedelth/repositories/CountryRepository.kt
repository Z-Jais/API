package fr.ziedelth.repositories

import fr.ziedelth.entities.Country
import org.hibernate.Session

class CountryRepository(session: Session) : AbstractRepository<Country>(session)