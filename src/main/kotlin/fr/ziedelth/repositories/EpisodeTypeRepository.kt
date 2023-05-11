package fr.ziedelth.repositories

import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.utils.Database

class EpisodeTypeRepository(database: Database) : AbstractRepository<EpisodeType>(database)
