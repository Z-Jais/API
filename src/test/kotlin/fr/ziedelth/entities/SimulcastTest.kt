package fr.ziedelth.entities

import org.junit.jupiter.api.Test
import kotlin.test.expect

class SimulcastTest {
    @Test
    fun hash() {
        val simulcast = Simulcast.getSimulcast(2022, 11)
        expect(2022) { simulcast.year }
        expect("AUTUMN") { simulcast.season }
    }
}