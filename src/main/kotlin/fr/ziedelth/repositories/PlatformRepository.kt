package fr.ziedelth.repositories

import fr.ziedelth.entities.Platform
import fr.ziedelth.utils.Database

class PlatformRepository(database: Database) : AbstractRepository<Platform>(database)