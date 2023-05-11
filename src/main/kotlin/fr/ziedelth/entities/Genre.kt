package fr.ziedelth.entities

import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.util.*

fun Genre?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(name = "genre")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class Genre(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val name: String? = null
) : Serializable {
    fun isNotValid(): Boolean = name.isNullOrBlank()
}
