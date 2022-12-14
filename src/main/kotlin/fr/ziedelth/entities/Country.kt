package fr.ziedelth.entities

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

fun Country?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(name = "country")
class Country(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val tag: String? = null,
    @Column(nullable = false, unique = true)
    val name: String? = null
) : Serializable {
    fun isNotValid(): Boolean = tag.isNullOrBlank() || name.isNullOrBlank()
}
