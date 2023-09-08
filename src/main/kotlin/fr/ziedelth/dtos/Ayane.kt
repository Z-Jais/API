package fr.ziedelth.dtos

import java.io.Serializable

data class Ayane(
    val message: String,
    val images: List<String>
) : Serializable
