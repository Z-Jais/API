package fr.ziedelth.dtos

import java.io.Serializable

data class AyaneDto(
    val message: String,
    val images: List<String>
) : Serializable
