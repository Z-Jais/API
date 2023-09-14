package fr.ziedelth.entities

import fr.ziedelth.utils.Constant
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
    }
}
