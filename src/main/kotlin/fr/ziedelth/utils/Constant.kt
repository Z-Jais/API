package fr.ziedelth.utils

import com.google.gson.Gson

object Constant {
    val gson = Gson()

    // Sort by year and season started by "Winter", "Spring", "Summer", "Autumn"
    val seasons = listOf("WINTER", "SPRING", "SUMMER", "AUTUMN")

    val secureKey: String? = System.getenv("SECURE_KEY")
}