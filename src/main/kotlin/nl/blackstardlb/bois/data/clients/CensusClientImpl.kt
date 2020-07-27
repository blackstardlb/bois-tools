package nl.blackstardlb.bois.data.clients

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.SocketTimeoutException
import java.net.URLDecoder
import java.net.URLEncoder

private val logger = KotlinLogging.logger {}

@Component
class CensusClientImpl(
        @Value("\${census.api_url}") val censusApiURL: String,
        private val client: HttpClient,
        private val objectMapper: ObjectMapper
) : CensusClient {
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun <T : Any> sendRequest(path: String, uriParameters: List<Pair<String, List<String>>>, clazz: Class<T>): T {
        return withContext(Dispatchers.IO) {
            val uri = URIBuilder("$censusApiURL$path").also { builder ->
                uriParameters.forEach { builder.addParameter(it.first, it.second.joinToString(",")) }
            }.build()

            val request = HttpGet(uri)
            request.config = RequestConfig.custom().setSocketTimeout(CensusClient.timeout.toMillis().toInt()).build()

            val entityString = async {
                logger.info { "--> ${URLDecoder.decode(uri.toString())}" }
                EntityUtils.toString(client.execute(request).entity)
            }
            objectMapper.readValue(entityString.await(), clazz)
        }
    }

    override fun shouldRetryOn(throwable: Throwable): Boolean {
        return throwable is SocketTimeoutException
    }
}