package fr.ziedelth.repositories

fun interface IPageRepository<T> {
    fun getByPage(tag: String, page: Int, limit: Int): List<T>
}