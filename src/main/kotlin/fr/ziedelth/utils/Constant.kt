package fr.ziedelth.utils

import com.google.gson.Gson

object Constant {
    val gson = Gson()

    // Sort by year and season started by "Winter", "Spring", "Summer", "Autumn"
    val seasons = listOf("WINTER", "SPRING", "SUMMER", "AUTUMN")

    val secureKey: String? = System.getenv("SECURE_KEY")
    val jwtAudience: String = System.getenv("JWT_AUDIENCE") ?: "jwtAudience"
    val jwtDomain: String = System.getenv("JWT_DOMAIN") ?: "jwtDomain"
    val jwtRealm: String = System.getenv("JWT_REALM") ?: "jwtRealm"
    val jwtSecret: String = System.getenv("JWT_SECRET") ?: "jwtSecret"
    const val JWT_TOKEN_TIMEOUT = 1 * 60 * 60 * 1000
}