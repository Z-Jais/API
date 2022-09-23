package fr.ziedelth.entities

import jakarta.persistence.*
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

fun Simulcast?.isNullOrNotValid() = this == null || this.isNotValid()

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("season", "year"))))
data class Simulcast(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val season: String? = null,
    @Column(nullable = false)
    val year: Int? = null
) : Serializable {
    fun isNotValid(): Boolean = season.isNullOrBlank() || year == null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Simulcast

        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , season = $season , year = $year )"
    }

    companion object {
        fun getSimulcast(year: Int, month: Int): Simulcast {
            val seasons = arrayOf("WINTER", "SPRING", "SUMMER", "AUTUMN")
            val season = seasons[(month - 1) / 3]
            return Simulcast(season = season, year = year)
        }
    }
}
