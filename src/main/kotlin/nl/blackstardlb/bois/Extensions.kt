package nl.blackstardlb.bois

import kotlin.math.ceil
import kotlin.math.min

fun <T> List<T>.split(size: Int): List<List<T>> {
    return (0 until (ceil(this.size / size.toDouble())).toInt()).map {
        val startFrom = it * size
        val end = min((it + 1) * size, this.size)
        this.subList(startFrom, end)
    }
}