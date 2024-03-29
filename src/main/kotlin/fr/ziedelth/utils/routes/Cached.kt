package fr.ziedelth.utils.routes

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Cached(val maxAgeSeconds: Int)
