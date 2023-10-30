package fr.ziedelth.utils.routes

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Path(val value: String = "")
