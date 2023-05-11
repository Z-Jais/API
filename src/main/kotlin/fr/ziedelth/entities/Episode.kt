package fr.ziedelth.entities

import fr.ziedelth.utils.DATE_FORMAT_REGEX
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.util.*

private const val COLLECTION_CACHE_REGION_NAME = "fr.ziedelth.entities.Episode"
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
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class Episode(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    platform: Platform? = null,
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "anime_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (anime_uuid) REFERENCES anime(uuid) ON DELETE CASCADE")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = COLLECTION_CACHE_REGION_NAME)
    var anime: Anime? = null,
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "episode_type_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (episode_type_uuid) REFERENCES episodetype(uuid) ON DELETE CASCADE")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = COLLECTION_CACHE_REGION_NAME)
    var episodeType: EpisodeType? = null,
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "lang_type_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (lang_type_uuid) REFERENCES langtype(uuid) ON DELETE CASCADE")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = COLLECTION_CACHE_REGION_NAME)
    var langType: LangType? = null,
    @Column(nullable = false, unique = true)
    val hash: String? = null,
    @Column(nullable = false)
    val releaseDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false)
    val season: Int? = null,
    @Column(nullable = false)
    var number: Int? = null,
    @Column(nullable = true)
    val title: String? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    val url: String? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    val image: String? = null,
    @Column(nullable = false)
    val duration: Long = -1
) : Platformeable(platform), Serializable {
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
