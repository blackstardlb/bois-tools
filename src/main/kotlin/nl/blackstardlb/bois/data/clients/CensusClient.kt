package nl.blackstardlb.bois.data.clients

import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.retry.Retry
import java.io.EOFException
import java.lang.Exception
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

interface CensusClient {
    companion object {
        val retrySpec = Retry.maxInARow(5)
    }
    fun <T : Any> sendRequest(path: String, uriParameters: List<Pair<String, List<String>>>, clazz: Class<T>): Mono<T>
    fun shouldRetryOn(throwable: Throwable): Boolean
}

inline fun <reified T : Any> CensusClient.sendRequestWithRetry(path: String, uriParameters: List<Pair<String, List<String>>>): Mono<T> {
    return this.sendRequest<T>(path, uriParameters).retryWhen(CensusClient.retrySpec.filter { this.shouldRetryOn(it) })
}

inline fun <reified T : Any> CensusClient.sendRequest(path: String, uriParameters: List<Pair<String, List<String>>>): Mono<T> {
    return this.sendRequest(path, uriParameters, T::class.java)
}