package nl.blackstardlb.bois

import mu.KLogger
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil
import kotlin.math.min

private val logger = KotlinLogging.logger {}

fun <T> List<T>.splitList(size: Int): List<List<T>> {
    return (0 until (ceil(this.size / size.toDouble())).toInt()).map {
        val startFrom = it * size
        val end = min((it + 1) * size, this.size)
        this.subList(startFrom, end)
    }
}

suspend fun <T> retryIO(
        times: Int,
        filter: (Throwable) -> Boolean = { true },
        block: suspend () -> T): T {
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Throwable) {
            if (!filter.invoke(e)) throw e
            logger.info { "Retrying call due to ${e.simpleErrorString()}" }
        }
    }
    return block()
}

suspend fun <T> timed(message: String = "function", aLogger: KLogger = logger, block: suspend () -> T): T {
    val now = Instant.now()
    try {
        val value = block()
        aLogger.info { "time: ${Duration.between(now, Instant.now()).pretty()} for $message" }
        return value
    } catch (e: Throwable) {
        aLogger.info { "time: ${Duration.between(now, Instant.now()).pretty()} for failed $message with ${e.simpleErrorString()}" }
        throw e
    }
}

fun Duration.pretty(): String {
    return this.toString()
            .substring(2)
            .replace("(\\d[HMS])(?!$)", "$1 ")
            .toLowerCase();
}

fun Throwable.simpleErrorString(): String {
    return "${this.javaClass.simpleName}(${this.message})"
}

fun Long.safeDivide(long: Long): Double {
    if (long == 0L) return 0.0
    return this / (long.toDouble())
}


fun Long.safeDivide(double: Double): Double {
    if (double == 0.0) return 0.0
    return this / (double)
}

fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()