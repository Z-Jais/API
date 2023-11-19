package fr.ziedelth.entities

import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.util.*

@Entity
@Table(
    name = "profile", indexes = [
        Index(name = "index_profile_token_uuid", columnList = "token_uuid", unique = true),
    ]
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "profile")
    @OrderBy("add_date")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    val animes: MutableSet<ProfileAnime> = mutableSetOf(),
    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "profile")
    @OrderBy("add_date")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    val episodes: MutableSet<ProfileEpisode> = mutableSetOf(),
) : Serializable