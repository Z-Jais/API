package fr.ziedelth.converters.profile

import fr.ziedelth.converters.AbstractConverter
import fr.ziedelth.dtos.ProfileDto
import fr.ziedelth.entities.Profile

class ProfileToProfileDtoConverter : AbstractConverter<Profile, ProfileDto>() {
    override fun convert(from: Profile): ProfileDto {
        val episodes = from.episodes.mapNotNull { it.episode }

        return ProfileDto(
            from.uuid,
            from.creationDate,
            from.lastUpdate,
            from.animes.map { it.anime!!.uuid }.toSet(),
            episodes.map { it.uuid }.toSet(),
            episodes.sumOf { if (it.duration > 0) it.duration else 0 },
        )
    }
}