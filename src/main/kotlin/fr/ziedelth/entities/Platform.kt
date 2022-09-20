package fr.ziedelth.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

@Entity
data class Platform(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val name: String? = null,
    @Column(nullable = false)
    val url: String? = null,
    @Column(nullable = false)
    val image: String? = null,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Platform

        return uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , name = $name , url = $url , image = $image )"
    }
}
