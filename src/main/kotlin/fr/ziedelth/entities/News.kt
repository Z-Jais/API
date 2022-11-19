package fr.ziedelth.entities

import fr.ziedelth.utils.DATE_FORMAT_REGEX
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

fun News?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
data class News(
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
        name = "country_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (country_uuid) REFERENCES country(uuid) ON DELETE CASCADE")
    )
    var country: Country? = null,
    @Column(nullable = false, unique = true)
    val hash: String? = null,
    @Column(nullable = false)
    val releaseDate: String = Calendar.getInstance().toISO8601(),
    @Column(nullable = false, columnDefinition = "TEXT")
    val title: String? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    val url: String? = null
) : Serializable {
    fun isNotValid(): Boolean =
        platform.isNullOrNotValid() || country.isNullOrNotValid() || hash.isNullOrBlank() || (
                releaseDate.isBlank() || !releaseDate.matches(DATE_FORMAT_REGEX)
                ) || title.isNullOrBlank() || description.isNullOrBlank() || url.isNullOrBlank()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as News

        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , platform = $platform , country = $country , hash = $hash , releaseDate = $releaseDate , title = $title , description = $description , url = $url )"
    }
}
