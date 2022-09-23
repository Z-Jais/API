package fr.ziedelth.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

fun Genre?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
data class Genre(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val name: String? = null
) : Serializable {
    fun isNotValid(): Boolean = name.isNullOrBlank()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Genre

        return uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , name = $name )"
    }
}
