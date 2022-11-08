package fr.ziedelth.entities

import jakarta.persistence.*
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

fun Country?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(name = "country")
data class Country(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val tag: String? = null,
    @Column(nullable = false, unique = true)
    val name: String? = null
) : Serializable {
    fun isNotValid(): Boolean = tag.isNullOrBlank() || name.isNullOrBlank()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Country

        return uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , tag = $tag , name = $name )"
    }
}
