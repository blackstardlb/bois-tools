package nl.blackstardlb.bois.config

import com.github.kittinunf.fuel.core.FuelManager
import nl.blackstardlb.bois.data.clients.CensusClientFuelImpl
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.JettyClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class CensusConfig(
        @Value("\${census.api_url}") val censusApiURL: String,
        @Value("\${webclient.max_memory_in_mb}") val maxWebClientMemoryInMb: Int
) {

    @Bean
    fun fuelManager(): FuelManager {
        val instance = FuelManager.instance
        instance.basePath = censusApiURL
        instance.addResponseInterceptor(CensusClientFuelImpl.responseLogger)
        instance.addRequestInterceptor(CensusClientFuelImpl.requestLogger)
        return instance
    }

    @Bean
    fun censusWebClient(): WebClient {
        val sslContextFactory: SslContextFactory.Client = SslContextFactory.Client()
        val httpClient: HttpClient = HttpClient(sslContextFactory).also {
            it.idleTimeout = 20000
            it.connectTimeout = 20000
        }
        val connector: ClientHttpConnector = JettyClientHttpConnector(httpClient)
        return WebClient
                .builder()
                .clientConnector(connector)
                .exchangeStrategies(ExchangeStrategies.builder().codecs { it.defaultCodecs().maxInMemorySize(maxWebClientMemoryInMb * 1024 * 1024) }.build())
                .baseUrl(censusApiURL)
                .build()
    }
}