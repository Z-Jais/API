package fr.ziedelth.entities

import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(
    name = "profile", indexes = [
        Index(name = "index_profile_token_uuid", columnList = "token_uuid", unique = true),
    ]
)
class Profile(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(name = "token_uuid", nullable = false, unique = true)
    val tokenUuid: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val creationDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false)
    var lastUpdate: String = Calendar.getInstance().toISO8601(),
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "profile")
    @OrderBy("add_date")
    val animes: MutableSet<ProfileAnime> = mutableSetOf(),
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "profile")
    @OrderBy("add_date")
    val episodes: MutableSet<ProfileEpisode> = mutableSetOf(),
) : Serializable