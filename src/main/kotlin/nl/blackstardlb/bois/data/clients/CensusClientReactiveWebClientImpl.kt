package nl.blackstardlb.bois.data.clients

import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.EOFException
import java.nio.charset.Charset
import java.util.concurrent.TimeoutException

class CensusClientReactiveWebClientImpl(private val client: WebClient) : CensusClient {
    override fun <T : Any> sendRequest(path: String, uriParameters: List<Pair<String, List<String>>>, clazz: Class<T>): Mono<T> {
        return client.get()
                .uri {
                    it.path(path)
                    uriParameters.forEach { parameter -> it.queryParam(parameter.first, parameter.second.joinToString(",")) }
                    it.build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(Charset.forName("UTF-8"))
                .retrieve()
                .bodyToMono(clazz)
    }

    override fun shouldRetryOn(throwable: Throwable): Boolean {
        return throwable is EOFException || throwable is TimeoutException
    }
}