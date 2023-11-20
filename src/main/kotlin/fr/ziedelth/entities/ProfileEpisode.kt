package fr.ziedelth.entities

import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "profile_episode")
class ProfileEpisode(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @ManyToOne
    @JoinColumn(
        name = "profile_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (profile_uuid) REFERENCES profile (uuid) ON DELETE CASCADE")
    )
    val profile: Profile? = null,
    @ManyToOne
    @JoinColumn(
        name = "episode_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (episode_uuid) REFERENCES episode (uuid) ON DELETE CASCADE")
    )
    val episode: Episode? = null,
    @Column(nullable = false, name = "add_date")
    val addDate: String = Calendar.getInstance().toISO8601(),
) : Serializable