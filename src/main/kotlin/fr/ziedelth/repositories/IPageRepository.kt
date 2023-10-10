package fr.ziedelth.repositories

import java.util.*

interface IPageRepository<T> {
    fun getByPage(tag: String, page: Int, limit: Int): List<T>
    fun getByPageWithAnime(uuid: UUID, page: Int, limit: Int): List<T>
}