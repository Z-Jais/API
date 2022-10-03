package fr.ziedelth.entities

import fr.ziedelth.utils.DATE_FORMAT_REGEX
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

fun Manga?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
data class Manga(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "platform_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (platform_uuid) REFERENCES platform(uuid) ON DELETE CASCADE")
    )
    var platform: Platform? = null,
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
) : Serializable {
    fun isNotValid(): Boolean =
        platform.isNullOrNotValid() || anime.isNullOrNotValid() || hash.isNullOrBlank() || (releaseDate.isBlank() || !releaseDate.matches(
            DATE_FORMAT_REGEX
        )) || url.isNullOrBlank() || cover.isNullOrBlank() || editor.isNullOrBlank()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Manga

        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , platform = $platform , anime = $anime , hash = $hash , releaseDate = $releaseDate , url = $url , cover = $cover , editor = $editor , ref = $ref , ean = $ean , age = $age , price = $price )"
    }
}
