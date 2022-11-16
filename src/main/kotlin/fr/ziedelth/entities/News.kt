package fr.ziedelth.entities

import fr.ziedelth.utils.DATE_FORMAT_REGEX
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import java.io.Serializable
import java.util.*

fun News?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(name = "news")
class News(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    platform: Platform? = null,
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "country_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (country_uuid) REFERENCES country(uuid) ON DELETE CASCADE")
    )
    var country: Country? = null,
    @Column(nullable = false, unique = true)
    val hash: String? = null,
    @Column(nullable = false)
    val releaseDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false)
    val title: String? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    val url: String? = null
) : Platformeable(platform), Serializable {
    fun isNotValid(): Boolean =
        platform.isNullOrNotValid() || country.isNullOrNotValid() || hash.isNullOrBlank() || (
                releaseDate.isBlank() || !releaseDate.matches(DATE_FORMAT_REGEX)
                ) || title.isNullOrBlank() || description.isNullOrBlank() || url.isNullOrBlank()
}
