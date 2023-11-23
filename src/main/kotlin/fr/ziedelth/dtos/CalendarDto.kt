package fr.ziedelth.dtos

import java.io.Serializable

data class CalendarDto(
    val message: String,
    val images: List<String>
) : Serializable
