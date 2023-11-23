package fr.ziedelth.services

import com.google.inject.Inject
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import fr.ziedelth.dtos.RecommendedAnimeDto
import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Genre
import fr.ziedelth.repositories.AnimeRepository
import java.io.File
import java.util.*

class RecommendationService {
    @Inject
    private lateinit var animeRepository: AnimeRepository

    fun getRecommendations(animes: List<Anime>): List<RecommendedAnimeDto> {
        val csvFile = File("data/anime-genres-themes.csv")
        val csvReader = CSVReaderBuilder(csvFile.reader()).withSkipLines(1).build()
        val allGenres = mutableSetOf<String>()
        val parsedAnimes = parseAnimeData(csvReader, allGenres)
        val availableGenres = allGenres.sorted()
        val intersectionAnimes = parsedAnimes.filter { anime -> animes.any { anime.uuid == it.uuid } }

        val animeGenreMatrices = intersectionAnimes.map { anime ->
            multiplyMatrix(
                buildAnimeGenreMatrix(
                    anime,
                    availableGenres
                ), 1.0
            )
        }

        val sum = animeGenreMatrices.sum(availableGenres.size)
        val normalized = normalizeMatrix(sum)
        val nonIntersectionAnimes = parsedAnimes.filter { anime -> intersectionAnimes.none { anime.uuid == it.uuid } }

        val sortedRecommendations = nonIntersectionAnimes.map { anime ->
            val matrix = buildAnimeGenreMatrix(anime, availableGenres)
            val result = multiplyMatrix(normalized, matrix)
            val matrixSum = result.sum()
            anime to matrixSum
        }.sortedByDescending { it.second }.toMutableSet()
        sortedRecommendations.removeIf { it.second == 0.0 }

        return sortedRecommendations.map { RecommendedAnimeDto(it.first, it.second) }
    }

    private fun parseAnimeData(
        csvReader: CSVReader,
        allGenres: MutableSet<String>
    ): MutableSet<Anime> {
        val parsedAnimes = mutableSetOf<Anime>()

        while (true) {
            val line = csvReader.readNext() ?: break
            val anime = animeRepository.find(UUID.fromString(line[0])) ?: continue
            val genres = line[3].split(",")
            val themes = line[4].split(",")

            val trimmedGenres = genres.map { it.trim().lowercase() }
            val trimmedThemes = themes.map { it.trim().lowercase() }
            val allAnimeGenres = trimmedGenres + trimmedThemes

            allGenres.addAll(allAnimeGenres)

            anime.genres.addAll(allAnimeGenres.map { Genre(name = it) })
            parsedAnimes.add(anime)
        }

        return parsedAnimes
    }

    private fun buildAnimeGenreMatrix(anime: Anime, genres: List<String>): DoubleArray {
        val matrix = DoubleArray(genres.size) { 0.0 }
        genres.forEachIndexed { index, genre -> matrix[index] = if (anime.genres.any { it.name == genre }) 1.0 else 0.0 }
        return matrix
    }

    private fun multiplyMatrix(matrix: DoubleArray, number: Double): DoubleArray {
        val outputMatrix = DoubleArray(matrix.size)
        matrix.forEachIndexed { index, d -> outputMatrix[index] = d * number }
        return outputMatrix
    }

    private fun multiplyMatrix(matrix1: DoubleArray, matrix2: DoubleArray): DoubleArray {
        if (matrix1.size != matrix2.size) throw Exception("Matrices are not of the same size")
        return matrix1.mapIndexed { index, value -> value * matrix2[index] }.toDoubleArray()
    }

    private fun List<DoubleArray>.sum(size: Int): DoubleArray {
        val sumArray = DoubleArray(size) { 0.0 }

        this.forEach { matrix ->
            matrix.forEachIndexed { index, value ->
                sumArray[index] += value
            }
        }

        return sumArray
    }

    private fun normalizeMatrix(matrix: DoubleArray): DoubleArray {
        val sum = matrix.sum()
        return matrix.map { it / sum }.toDoubleArray()
    }
}