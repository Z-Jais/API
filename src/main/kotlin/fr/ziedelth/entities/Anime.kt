package fr.ziedelth.entities

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
    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "country_uuid", nullable = false)
    var country: Country? = null,
    @Column(nullable = false, unique = true)
    val name: String? = null,
    @Column(nullable = false)
    val releaseDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false)
    val image: String? = null,
    @Column(nullable = true)
    val description: String? = null,
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name = "anime_hash", joinColumns = [JoinColumn(name = "anime_uuid")])
    val hashes: MutableList<String> = mutableListOf(),
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
        name = "anime_genre",
        joinColumns = [JoinColumn(name = "anime_uuid")],
        inverseJoinColumns = [JoinColumn(name = "genre_uuid")]
    )
    val genres: MutableList<Genre> = mutableListOf()
) : Serializable {
    fun hash(): String? = name?.lowercase()?.filter { it.isLetterOrDigit() || it.isWhitespace() || it == '-' }?.trim()?.replace("\\s+".toRegex(), "-")?.replace("--", "-")

    fun isNotValid(): Boolean = country.isNullOrNotValid() || name.isNullOrBlank() || (releaseDate.isBlank() || !releaseDate.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z\$".toRegex())) || image.isNullOrBlank()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Anime

        return uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , country = $country , name = $name , releaseDate = $releaseDate , image = $image , description = $description , hashes = $hashes )"
    }
}
