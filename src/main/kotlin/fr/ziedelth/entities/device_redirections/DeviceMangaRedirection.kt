package fr.ziedelth.entities.device_redirections

import fr.ziedelth.entities.Device
import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Manga
import jakarta.persistence.*
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "device_manga_redirection")
data class DeviceMangaRedirection(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    var timestamp: Calendar = Calendar.getInstance(),
    @ManyToOne(optional = false)
    val device: Device? = null,
    @ManyToOne(optional = false)
    val manga: Manga? = null,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as DeviceMangaRedirection

        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , timestamp = $timestamp , device = $device , manga = $manga )"
    }
}
