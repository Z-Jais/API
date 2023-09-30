package fr.ziedelth.entities

import fr.ziedelth.utils.CalendarConverter
import fr.ziedelth.utils.Constant
import fr.ziedelth.utils.toISO8601
import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "simulcast", uniqueConstraints = [UniqueConstraint(columnNames = arrayOf("season", "\"year\""))])
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
            val season = Constant.seasons[(month - 1) / 3]
            return Simulcast(season = season, year = year)
        }

        fun getSimulcastFrom(date: String): Simulcast {
            val calendar = CalendarConverter.toUTCCalendar(date)
            val iso8601 = calendar.toISO8601()
            val split = iso8601.split("-")
            return getSimulcast(split[0].toInt(), split[1].toInt())
        }
    }

    override fun toString(): String {
        return "Simulcast(uuid=$uuid, season=$season, year=$year)"
    }

    fun equalsWithoutUUID(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Simulcast

        if (season != other.season) return false
        if (year != other.year) return false

        return true
    }
}
