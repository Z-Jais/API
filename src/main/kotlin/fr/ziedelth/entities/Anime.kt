package fr.ziedelth.entities

import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import org.hibernate.Hibernate
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import java.io.Serializable
import java.util.*

@Entity
data class Anime(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @ManyToOne(cascade = [CascadeType.REMOVE], fetch = FetchType.EAGER)
    @JoinColumn(name = "country_uuid", nullable = false)
    val country: Country? = null,
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
    @OneToMany(cascade = [CascadeType.REMOVE], fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
        name = "anime_genre",
        joinColumns = [JoinColumn(name = "anime_uuid")],
        inverseJoinColumns = [JoinColumn(name = "genre_uuid")]
    )
    val genres: MutableList<Genre> = mutableListOf()
) : Serializable {
    init {
        if (hashes.isEmpty()) {
            name?.lowercase()?.filter { it.isLetterOrDigit() || it.isWhitespace() || it == '-' }?.trim()
                ?.replace("\\s+".toRegex(), "-")?.replace("--", "-")?.let { hashes.add(it) }
        }
    }

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
