package fr.ziedelth.repositories

import fr.ziedelth.entities.Country
import fr.ziedelth.utils.Database

class CountryRepository(database: Database) : AbstractRepository<Country>(database)