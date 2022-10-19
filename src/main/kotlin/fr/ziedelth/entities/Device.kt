package fr.ziedelth.entities

import jakarta.persistence.*
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

fun Device?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
data class Device(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    var name: String? = null,
    @Column(nullable = false)
    var os: String? = null,
    @Column(nullable = false)
    var model: String? = null,
    @Column(nullable = false)
    val createdAt: Calendar = Calendar.getInstance(),
    @Column(nullable = false)
    var updatedAt: Calendar = Calendar.getInstance(),
) : Serializable {
    fun isNotValid(): Boolean = name.isNullOrBlank() || os.isNullOrBlank() || model.isNullOrBlank()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Device

        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , name = $name , os = $os , model = $model , createdAt = $createdAt , updatedAt = $updatedAt )"
    }
}
