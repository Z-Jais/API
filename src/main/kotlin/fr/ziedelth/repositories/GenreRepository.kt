package fr.ziedelth.repositories

import fr.ziedelth.entities.Genre
import fr.ziedelth.utils.Database

class GenreRepository(database: Database) : AbstractRepository<Genre>(database)