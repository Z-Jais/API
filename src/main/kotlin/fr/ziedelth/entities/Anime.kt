package fr.ziedelth.entities

import fr.ziedelth.utils.DATE_FORMAT_REGEX
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import org.hibernate.Hibernate
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import java.io.Serializable
import java.util.*

fun Anime?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
data class Anime(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "country_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (country_uuid) REFERENCES country (uuid) ON DELETE CASCADE")
    )
    var country: Country? = null,
    @Column(nullable = false)
    val name: String? = null,
    @Column(nullable = false)
    val releaseDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false, columnDefinition = "TEXT")
    val image: String? = null,
    @Column(nullable = true, columnDefinition = "TEXT")
    val description: String? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(
        name = "anime_hash",
        joinColumns = [JoinColumn(name = "anime_uuid")],
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (anime_uuid) REFERENCES anime (uuid) ON DELETE CASCADE")
    )
    val hashes: MutableSet<String> = mutableSetOf(),
    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
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
    val genres: MutableSet<Genre> = mutableSetOf(),
    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
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
    val simulcasts: MutableSet<Simulcast> = mutableSetOf(),
) : Serializable {
    fun hash(): String? = name?.lowercase()?.filter { it.isLetterOrDigit() || it.isWhitespace() || it == '-' }?.trim()
        ?.replace("\\s+".toRegex(), "-")?.replace("--", "-")

    fun isNotValid(): Boolean = country.isNullOrNotValid() || name.isNullOrBlank() || (
            releaseDate.isBlank() || !releaseDate.matches(
                DATE_FORMAT_REGEX
            )
            ) || image.isNullOrBlank()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Anime

        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , country = $country , name = $name , releaseDate = $releaseDate , image = $image , description = $description , hashes = $hashes )"
    }
}
