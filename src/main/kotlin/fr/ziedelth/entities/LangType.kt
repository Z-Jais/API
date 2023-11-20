package fr.ziedelth.entities

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

fun LangType?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(name = "langtype")
class LangType(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val name: String? = null
) : Serializable {
    fun isNotValid(): Boolean = name.isNullOrBlank()
}
