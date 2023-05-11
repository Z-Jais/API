package fr.ziedelth.utils

import java.io.File

class DatabaseTest(file: File) : Database(file) {
    constructor() : this(
        File(
            ClassLoader.getSystemClassLoader().getResource("hibernate.cfg.xml")?.file
                ?: throw Exception("hibernate.cfg.xml not found")
        )
    )

    fun clean() {
        inTransaction { session ->
            getEntities().forEach { session.createQuery("DELETE FROM ${it.simpleName}").executeUpdate() }
        }
    }
}