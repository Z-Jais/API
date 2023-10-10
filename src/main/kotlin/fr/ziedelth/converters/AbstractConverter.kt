package fr.ziedelth.converters

import org.reflections.Reflections
import java.lang.reflect.ParameterizedType

abstract class AbstractConverter<F, T> {
    abstract fun convert(from: F): T

    companion object {
        private val converters: MutableMap<Pair<Class<*>, Class<*>>, AbstractConverter<*, *>> = mutableMapOf()

        init {
            val converters = Reflections("fr.ziedelth.converters").getSubTypesOf(AbstractConverter::class.java)

            converters.forEach {
                val (from, to) = (it.genericSuperclass as ParameterizedType).actualTypeArguments.map { argument -> argument as Class<*> }
                this.converters[Pair(from, to)] = it.getConstructor().newInstance()
            }
        }

        fun <T> convert(`object`: Any, to: Class<T>): T {
            val pair = Pair(`object`.javaClass, to)

            if (!converters.containsKey(pair)) {
                throw NoSuchElementException("Can not find converter \"${`object`.javaClass.simpleName}\" to \"${to.simpleName}\"")
            }

            val abstractConverter = converters[pair] ?: throw IllegalStateException()
            val abstractConverterClass = abstractConverter.javaClass
            val method = abstractConverterClass.getMethod("convert", `object`.javaClass)
            method.isAccessible = true
            return method.invoke(abstractConverter, `object`) as T
        }
    }
}