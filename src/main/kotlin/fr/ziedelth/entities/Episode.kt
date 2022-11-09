package fr.ziedelth.entities

import fr.ziedelth.utils.DATE_FORMAT_REGEX
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import java.io.Serializable
import java.util.*

fun Episode?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(name = "episode")
class Episode(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    platform: Platform? = null,
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "anime_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (anime_uuid) REFERENCES anime(uuid) ON DELETE CASCADE")
    )
    var anime: Anime? = null,
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "episode_type_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (episode_type_uuid) REFERENCES episodetype(uuid) ON DELETE CASCADE")
    )
    var episodeType: EpisodeType? = null,
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
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
