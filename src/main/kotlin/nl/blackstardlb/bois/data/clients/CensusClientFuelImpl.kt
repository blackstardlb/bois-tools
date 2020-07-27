package nl.blackstardlb.bois.data.clients

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.reactor.monoObject
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.SocketTimeoutException

private val logger = KotlinLogging.logger {}

@Component
class CensusClientFuelImpl(
        val mapper: ObjectMapper,
        val fuelManager: FuelManager
) : CensusClient {
    companion object {
        val responseLogger: FoldableResponseInterceptor = object : FoldableResponseInterceptor {
            override fun invoke(p1: ResponseTransformer): ResponseTransformer {
                return { _, response ->
                    logger.debug { response.toString() }
                    response
                }
            }
        }
        val requestLogger: FoldableRequestInterceptor = object : FoldableRequestInterceptor {
            override fun invoke(p1: RequestTransformer): RequestTransformer {
                return { request ->
                    logger.info { "${request.method} : ${request.url}" }
                    logger.debug { request.toString() }
                    request
                }
            }
        }
    }

    override fun <T : Any> sendRequest(path: String, uriParameters: List<Pair<String, List<String>>>, clazz: Class<T>) = mono {
        val parameters = uriParameters.map { Pair(it.first, it.second.joinToString(",") { s -> s }) }
        val deserializer = object : ResponseDeserializable<T> {
            override fun deserialize(content: String): T? {
                return mapper.readValue<T>(content, clazz)
            }
        }
        return@mono fuelManager.get(path, parameters).awaitObject(deserializer)
    }

    override fun shouldRetryOn(throwable: Throwable): Boolean {
        if (throwable is FuelError) {
            return throwable.exception is SocketTimeoutException
        }
        return false
    }
}