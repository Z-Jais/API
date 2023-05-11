package fr.ziedelth.entities

import fr.ziedelth.utils.DATE_FORMAT_REGEX
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.util.*

private const val COLLECTION_CACHE_REGION_NAME = "fr.ziedelth.entities.Anime"
fun Anime?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(
    name = "anime",
    indexes = [
        Index(name = "index_anime_country_uuid", columnList = "country_uuid"),
        Index(name = "index_anime_release_date", columnList = "releasedate")
    ]
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Anime(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE, CascadeType.PERSIST])
    @JoinColumn(
        name = "country_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (country_uuid) REFERENCES country (uuid) ON DELETE CASCADE")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = COLLECTION_CACHE_REGION_NAME)
    var country: Country? = null,
    @Column(nullable = false)
    val name: String? = null,
    @Column(nullable = false)
    val releaseDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false, columnDefinition = "TEXT")
    val image: String? = null,
    @Column(nullable = true, columnDefinition = "TEXT")
    val description: String? = null,
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "anime_hash",
        joinColumns = [JoinColumn(name = "anime_uuid")],
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (anime_uuid) REFERENCES anime (uuid) ON DELETE CASCADE")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = COLLECTION_CACHE_REGION_NAME)
    val hashes: MutableSet<String> = mutableSetOf(),
    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE, CascadeType.PERSIST])
    @JoinTable(
        name = "anime_genre",
        joinColumns = [
            JoinColumn(
                name = "anime_uuid",
                foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (anime_uuid) REFERENCES anime (uuid) ON DELETE CASCADE")
            )
        ],
        inverseJoinColumns = [
            JoinColumn(
                name = "genre_uuid",
                foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (genre_uuid) REFERENCES genre (uuid) ON DELETE CASCADE")
            )
        ]
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = COLLECTION_CACHE_REGION_NAME)
    val genres: MutableSet<Genre> = mutableSetOf(),
    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE, CascadeType.PERSIST])
    @JoinTable(
        name = "anime_simulcast",
        joinColumns = [
            JoinColumn(
                name = "anime_uuid",
                foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (anime_uuid) REFERENCES anime (uuid) ON DELETE CASCADE")
            )
        ],
        inverseJoinColumns = [
            JoinColumn(
                name = "simulcast_uuid",
                foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (simulcast_uuid) REFERENCES simulcast (uuid) ON DELETE CASCADE")
            )
        ]
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = COLLECTION_CACHE_REGION_NAME)
    val simulcasts: MutableSet<Simulcast> = mutableSetOf(),
) : Serializable {
    fun hash(): String = name!!.lowercase().filter { it.isLetterOrDigit() || it.isWhitespace() || it == '-' }.trim()
        .replace("\\s+".toRegex(), "-").replace("--", "-")

    fun isNotValid(): Boolean = country.isNullOrNotValid() || name.isNullOrBlank() || (
            releaseDate.isBlank() || !releaseDate.matches(
                DATE_FORMAT_REGEX
            )
            ) || image.isNullOrBlank()
}
