package nl.blackstardlb.bois.data.clients

import mu.KotlinLogging
import nl.blackstardlb.bois.retryIO
import nl.blackstardlb.bois.timed
import org.apache.http.client.utils.URIBuilder
import java.time.Duration

interface CensusClient {
    companion object {
        val timeout: Duration = Duration.ofSeconds(20)
    }

    suspend fun <T : Any> sendRequest(path: String, uriParameters: List<Pair<String, List<String>>>, clazz: Class<T>): T
    fun shouldRetryOn(throwable: Throwable): Boolean
}

suspend inline fun <reified T : Any> CensusClient.sendRequestWithRetry(path: String, uriParameters: List<Pair<String, List<String>>>): T {
    return retryIO(5, { shouldRetryOn(it) }) { this.sendRequest<T>(path, uriParameters) }
}

suspend inline fun <reified T : Any> CensusClient.sendRequest(path: String, uriParameters: List<Pair<String, List<String>>>): T {
    val uri = URIBuilder(path).also { builder ->
        uriParameters.forEach { builder.addParameter(it.first, it.second.joinToString(",")) }
    }.build()
    return timed(uri.toString(), KotlinLogging.logger { }) { this.sendRequest(path, uriParameters, T::class.java) }
}