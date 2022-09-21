package fr.ziedelth.entities

import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

@Entity
data class Episode(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @ManyToOne(cascade = [CascadeType.REMOVE], fetch = FetchType.EAGER)
    @JoinColumn(name = "platform_uuid", nullable = false)
    val platform: Platform? = null,
    @ManyToOne(cascade = [CascadeType.REMOVE], fetch = FetchType.EAGER)
    @JoinColumn(name = "anime_uuid", nullable = false)
    val anime: Anime? = null,
    @ManyToOne(cascade = [CascadeType.REMOVE], fetch = FetchType.EAGER)
    @JoinColumn(name = "episode_type_uuid", nullable = false)
    val episodeType: EpisodeType? = null,
    @ManyToOne(cascade = [CascadeType.REMOVE], fetch = FetchType.EAGER)
    @JoinColumn(name = "lang_type_uuid", nullable = false)
    val langType: LangType? = null,
    @Column(nullable = false, unique = true)
    val hash: String? = null,
    @Column(nullable = false)
    val releaseDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false)
    val season: Int? = null,
    @Column(nullable = false)
    val number: Int? = null,
    @Column(nullable = true)
    val title: String? = null,
    @Column(nullable = false)
    val url: String? = null,
    @Column(nullable = false)
    val image: String? = null,
    @Column(nullable = false)
    val duration: Long = -1
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Episode

        return uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , platform = $platform , anime = $anime , episodeType = $episodeType , langType = $langType , hash = $hash , releaseDate = $releaseDate , season = $season , number = $number , title = $title , url = $url , image = $image , duration = $duration )"
    }
}
