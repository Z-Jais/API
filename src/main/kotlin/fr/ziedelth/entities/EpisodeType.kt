package fr.ziedelth.entities

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

fun EpisodeType?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(name = "episodetype")
class EpisodeType(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val name: String? = null
) : Serializable {
    fun isNotValid(): Boolean = name.isNullOrBlank()
}
