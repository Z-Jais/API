package fr.ziedelth.entities

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "simulcast", uniqueConstraints = [UniqueConstraint(columnNames = arrayOf("season", "year"))])
class Simulcast(
    @Id
    @GeneratedValue
    val uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val season: String? = null,
    @Column(nullable = false, name = "\"year\"")
    val year: Int? = null
) : Serializable {
    companion object {
        fun getSimulcast(year: Int, month: Int): Simulcast {
            val seasons = arrayOf("WINTER", "SPRING", "SUMMER", "AUTUMN")
            val season = seasons[(month - 1) / 3]
            return Simulcast(season = season, year = year)
        }
    }
}
