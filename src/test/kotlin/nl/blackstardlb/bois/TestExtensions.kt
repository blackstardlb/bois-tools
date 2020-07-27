package nl.blackstardlb.bois

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

fun <T> runTesting(block: suspend CoroutineScope.() -> T): Unit {
    return runBlocking {
        block.invoke(this)
        return@runBlocking
    }
}