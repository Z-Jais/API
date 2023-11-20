package fr.ziedelth.entities

import fr.ziedelth.utils.DATE_FORMAT_REGEX
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import java.io.Serializable
import java.util.*

fun Episode?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(
    name = "episode",
    indexes = [
        Index(name = "index_episode_anime_uuid", columnList = "anime_uuid"),
        Index(name = "index_episode_platform_uuid", columnList = "platform_uuid"),
        Index(name = "index_episode_episode_type_uuid", columnList = "episode_type_uuid"),
        Index(name = "index_episode_lang_type_uuid", columnList = "lang_type_uuid"),
        Index(name = "index_episode_release_date", columnList = "releasedate")
    ]
)
class Episode(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "platform_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (platform_uuid) REFERENCES platform(uuid) ON DELETE CASCADE")
    )
    var platform: Platform? = null,
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "anime_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (anime_uuid) REFERENCES anime(uuid) ON DELETE CASCADE")
    )
    var anime: Anime? = null,
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "episode_type_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (episode_type_uuid) REFERENCES episodetype(uuid) ON DELETE CASCADE")
    )
    var episodeType: EpisodeType? = null,
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "lang_type_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (lang_type_uuid) REFERENCES langtype(uuid) ON DELETE CASCADE")
    )
    var langType: LangType? = null,
    @Column(nullable = false, unique = true)
    val hash: String? = null,
    @Column(nullable = false)
    val releaseDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false)
    var season: Int? = null,
    @Column(nullable = false)
    var number: Int? = null,
    @Column(nullable = true)
    val title: String? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    val url: String? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    val image: String? = null,
    @Column(nullable = false)
    var duration: Long = -1
) : Serializable {
    fun isNotValid(): Boolean =
        platform.isNullOrNotValid() || anime.isNullOrNotValid() || episodeType.isNullOrNotValid() || langType.isNullOrNotValid() || hash.isNullOrBlank() || (
                releaseDate.isBlank() || !releaseDate.matches(DATE_FORMAT_REGEX)
                ) || season == null || number == null || url.isNullOrBlank() || image.isNullOrBlank()

    fun copy(anime: Anime? = this.anime) = Episode(
        this.uuid,
        platform,
        anime,
        episodeType,
        langType,
        hash,
        releaseDate,
        season,
        number,
        title,
        url,
        image,
        duration
    )
}
