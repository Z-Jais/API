package fr.ziedelth.entities

import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.util.*

fun Platform?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(name = "platform")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class Platform(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val name: String? = null,
    @Column(nullable = false)
    val url: String? = null,
    @Column(nullable = false)
    val image: String? = null
) : Serializable {
    fun isNotValid(): Boolean = name.isNullOrBlank() || url.isNullOrBlank() || image.isNullOrBlank()
}
