package fr.ziedelth.entities

import fr.ziedelth.utils.DATE_FORMAT_REGEX
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import java.io.Serializable
import java.util.*

fun Manga?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(name = "manga")
class Manga(
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
    @Column(nullable = false, unique = true)
    val hash: String? = null,
    @Column(nullable = false)
    val releaseDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false)
    val url: String? = null,
    @Column(nullable = false)
    val cover: String? = null,
    @Column(nullable = false)
    val editor: String? = null,
    @Column(nullable = true)
    val ref: String? = null,
    @Column(nullable = true)
    val ean: Long? = null,
    @Column(nullable = true)
    val age: Int? = null,
    @Column(nullable = true)
    val price: Double? = null
) : Platformeable(platform), Serializable {
    fun isNotValid(): Boolean =
        platform.isNullOrNotValid() || anime.isNullOrNotValid() || hash.isNullOrBlank() || (releaseDate.isBlank() || !releaseDate.matches(
            DATE_FORMAT_REGEX
        )) || url.isNullOrBlank() || cover.isNullOrBlank() || editor.isNullOrBlank()

    fun copy(anime: Anime? = this.anime) = Manga(
        uuid = uuid,
        platform = platform,
        anime = anime,
        hash = hash,
        releaseDate = releaseDate,
        url = url,
        cover = cover,
        editor = editor,
        ref = ref,
        ean = ean,
        age = age,
        price = price
    )
}
